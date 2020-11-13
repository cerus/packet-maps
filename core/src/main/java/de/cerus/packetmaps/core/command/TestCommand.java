package de.cerus.packetmaps.core.command;

import com.madgag.gif.fmsware.GifDecoder;
import de.cerus.jgif.GifImage;
import de.cerus.packetmaps.core.factory.DrawAdapterFactory;
import de.cerus.packetmaps.core.gif.task.GifRendererTask;
import de.cerus.packetmaps.core.pmcache.GifCache;
import de.cerus.packetmaps.nmsbase.DrawAdapter;
import de.cerus.packetmaps.nmsbase.FakeMap;
import de.cerus.packetmaps.nmsbase.MapIcon;
import de.cerus.packetmaps.nmsbase.NmsAdapter;
import de.cerus.packetmaps.nmsbase.screen.MapScreen;
import de.cerus.packetmaps.nmsbase.screen.ScreenGraphics;
import de.cerus.packetmaps.v1_16_3.DrawAdapterImpl;
import de.cerus.packetmaps.v1_16_3.MapPalette;
import de.cerus.packetmaps.v1_16_3.NmsAdapterImpl;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TestCommand implements CommandExecutor {

    private final NmsAdapter nmsAdapter;

    public TestCommand(final DrawAdapterFactory drawAdapter, final NmsAdapter nmsAdapter) {
        this.nmsAdapter = nmsAdapter;
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        if (!(commandSender instanceof Player)) {
            return true;
        }
        final Player player = (Player) commandSender;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("makemap2")) {
                final int id = Integer.parseInt(args[1]);

                final DrawAdapterImpl drawAdapter = new DrawAdapterImpl();
                drawAdapter.fill(this.nmsAdapter.matchRgb(0, 0, 0));
                drawAdapter.line(10, 10, 117, 117, this.nmsAdapter.matchRgb(255, 255, 255));
                drawAdapter.line(10, 117, 117, 10, this.nmsAdapter.matchRgb(255, 255, 255));

                final Object o = this.nmsAdapter.constructMapPacket(id, (byte) 0, false, true, new ArrayList<>(), drawAdapter.toArray());
                this.nmsAdapter.sendPacket(player, o);
            } else if (args[0].equalsIgnoreCase("makescreen2")) {
                final List<Entity> frames = player.getNearbyEntities(10, 10, 10).stream()
                        .filter(entity -> entity instanceof ItemFrame)
                        .sorted(Comparator.comparingInt(value -> ((Entity) value).getLocation().getBlockX()).reversed()
                                .thenComparing(Comparator.comparingInt(value -> ((Entity) value).getLocation().getBlockY()).reversed()))
                        .collect(Collectors.toList());

                final GifImage gifImage;
                try {
                    gifImage = new GifImage(new File("E:\\Minecraft-Server\\Paper-1.16.3\\plugins\\test_gif_big.gif"));
                } catch (final FileNotFoundException e) {
                    e.printStackTrace();
                    return true;
                }

                final int col = Integer.parseInt(args[1]);
                final int row = Integer.parseInt(args[2]);

                final Entity[][] entities = new Entity[col][row];
                int n = 0;
                for (int i = 0; i < col; i++) {
                    for (int i1 = 0; i1 < row; i1++) {
                        entities[i][i1] = frames.get(n++);
                    }
                }

                final MapScreen mapScreen = new MapScreen(col, row, entities, new NmsAdapterImpl());
                mapScreen.addObserver(player);
                mapScreen.update();
                mapScreen.send();

                final JavaPlugin plugin = JavaPlugin.getPlugin(JavaPlugin.class);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    player.sendMessage("wait..");
                    final GifCache gifCache = GifCache.buildCache(gifImage, new Dimension(gifImage.getFirstFrame().getWidth(), gifImage.getFirstFrame().getHeight()), this.nmsAdapter);
                    gifCache.resize(mapScreen.getTotalDimensions());
                    final GifRendererTask gifRendererTask = new GifRendererTask(mapScreen, gifCache);
                    player.sendMessage("ok");

                    final AtomicInteger count = new AtomicInteger(0);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            gifRendererTask.run();

                            if (count.incrementAndGet() >= 300) {
                                this.cancel();
                            }
                        }
                    }.runTaskTimerAsynchronously(plugin, 0, 2);
                });
            } else if (args[0].equalsIgnoreCase("makescreen")) {
                final List<Entity> frames = player.getNearbyEntities(10, 10, 10).stream()
                        .filter(entity -> entity instanceof ItemFrame)
                        .sorted(Comparator.comparingInt(value -> ((Entity) value).getLocation().getBlockX()).reversed()
                                .thenComparing(Comparator.comparingInt(value -> ((Entity) value).getLocation().getBlockY()).reversed()))
                        .collect(Collectors.toList());

                final int col = Integer.parseInt(args[1]);
                final int row = Integer.parseInt(args[2]);

                final Entity[][] entities = new Entity[col][row];
                int n = 0;
                for (int i = 0; i < col; i++) {
                    for (int i1 = 0; i1 < row; i1++) {
                        entities[i][i1] = frames.get(n++);
                    }
                }

                final MapScreen mapScreen = new MapScreen(col, row, entities, new NmsAdapterImpl());
                mapScreen.addObserver(player);
                mapScreen.update();
                mapScreen.send();

                final GifImage gifImage;
                try {
                    gifImage = new GifImage(new File("E:\\Minecraft-Server\\Paper-1.16.3\\plugins\\test_gif_big.gif"));
                } catch (final FileNotFoundException e) {
                    e.printStackTrace();
                    return true;
                }

                final JavaPlugin plugin = JavaPlugin.getPlugin(JavaPlugin.class);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    final ScreenGraphics graphics = mapScreen.getScreenGraphics();
                    graphics.drawImage(gifImage.getFirstFrame(), 0, 0, 0, 0, gifImage.getFirstFrame().getWidth(), gifImage.getFirstFrame().getHeight());
                    mapScreen.update();
                }, 20);
            } else if (args[0].equalsIgnoreCase("makeframes3")) {
                final GifImage gifImage;
                try {
                    gifImage = new GifImage(new File("E:\\Minecraft-Server\\Paper-1.16.3\\plugins\\test_gif.gif"));
                } catch (final FileNotFoundException e) {
                    e.printStackTrace();
                    return true;
                }

                final List<Entity> frames = player.getNearbyEntities(10, 10, 10).stream()
                        .filter(entity -> entity instanceof ItemFrame)
                        .sorted(Comparator.comparingInt(value -> ((Entity) value).getLocation().getBlockX()).reversed())
                        .sorted(Comparator.comparingInt(value -> ((Entity) value).getLocation().getBlockY()).reversed())
                        .collect(Collectors.toList());

                final FakeMap fakeMap = new FakeMap(this.nmsAdapter, new DrawAdapterImpl());
                fakeMap.addObserver(player);
                fakeMap.send();

                frames.forEach(entity ->
                        this.nmsAdapter.sendPacket(player, this.nmsAdapter.constructFramePacket(entity.getEntityId(), fakeMap.getId())));

                final AtomicInteger count = new AtomicInteger(0);
                final AtomicInteger index = new AtomicInteger(0);
                final int size = gifImage.getFrames().size();

                int avgDelay = 0;
                final GifDecoder decoder = gifImage.getDecoder();
                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    avgDelay += decoder.getDelay(i);
                }
                avgDelay = avgDelay / decoder.getFrameCount();
                player.sendMessage("avg: " + avgDelay);

                final DrawAdapter[] adapters = new DrawAdapter[decoder.getFrameCount()];
                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    final DrawAdapter adapter = new DrawAdapterImpl();
                    adapter.drawImage(decoder.getFrame(i));
                    adapters[i] = adapter;
                }
                final byte[][] cache = new byte[adapters.length][];
                for (int i = 0; i < adapters.length; i++) {
                    cache[i] = adapters[i].toSingleArray();
                    player.sendMessage("cached i w/len " + cache[i].length);
                }

                final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                executorService.scheduleAtFixedRate(() -> {
                    int idx = index.getAndIncrement();
                    if (idx >= size) {
                        idx = 0;
                        index.set(0);
                    }

                    fakeMap.setCachedData(cache[idx]);
                    fakeMap.sendCached();

                    if (count.getAndIncrement() >= 500) {
                        executorService.shutdown();
                    }
                }, 0, avgDelay, TimeUnit.MILLISECONDS);
            } else if (args[0].equalsIgnoreCase("makeframes2")) {
                final List<Entity> frames = player.getNearbyEntities(10, 10, 10).stream()
                        .filter(entity -> entity instanceof ItemFrame)
                        .sorted(Comparator.comparingInt(value -> ((Entity) value).getLocation().getBlockX()).reversed())
                        .sorted(Comparator.comparingInt(value -> ((Entity) value).getLocation().getBlockY()).reversed())
                        .collect(Collectors.toList());

                final FakeMap fakeMap = new FakeMap(this.nmsAdapter, new DrawAdapterImpl());
                fakeMap.addObserver(player);
                fakeMap.send();

                frames.forEach(entity ->
                        this.nmsAdapter.sendPacket(player, this.nmsAdapter.constructFramePacket(entity.getEntityId(), fakeMap.getId())));

                final JavaPlugin plugin = JavaPlugin.getPlugin(JavaPlugin.class);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    final DrawAdapterImpl adapter1 = new DrawAdapterImpl();
                    adapter1.line(0, 0, 0, 5, (byte) MapPalette.BLACK_3.getId());
                    adapter1.line(1, 0, 1, 5, (byte) MapPalette.WHITE2_0.getId());
                    adapter1.line(2, 0, 2, 5, (byte) MapPalette.INDIGO_0.getId());

                    fakeMap.sendAndSetChanges(adapter1);
                }, 3 * 20);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    final DrawAdapterImpl adapter2 = new DrawAdapterImpl();
                    adapter2.line(3, 0, 3, 5, (byte) MapPalette.BLUE2_0.getId());
                    adapter2.line(4, 0, 4, 5, (byte) MapPalette.GREEN2_0.getId());
                    adapter2.line(5, 0, 5, 5, (byte) MapPalette.CYAN_0.getId());

                    fakeMap.sendAndSetChanges(adapter2);
                }, 6 * 20);
            } else if (args[0].equalsIgnoreCase("makeframes")) {
                final List<Entity> frames = player.getNearbyEntities(10, 10, 10).stream()
                        .filter(entity -> entity instanceof ItemFrame)
                        .sorted(Comparator.comparingInt(value -> ((Entity) value).getLocation().getBlockX()).reversed())
                        .sorted(Comparator.comparingInt(value -> ((Entity) value).getLocation().getBlockY()).reversed())
                        .collect(Collectors.toList());

                final BufferedImage bufferedImage;
                try {
                    bufferedImage = ImageIO.read(new URL("http://www.simpleimageresizer.com/_uploads/photos/725e4174/Hypixel_5_87.jpg"));
                } catch (final IOException e) {
                    e.printStackTrace();
                    return true;
                }

                int currX = 0;
                int currZ = 0;
                for (final Entity entity : frames) {
                    if (currX >= bufferedImage.getWidth() && currZ >= bufferedImage.getHeight()) {
                        continue;
                    }

                    final DrawAdapterImpl drawAdapter = new DrawAdapterImpl();
                    drawAdapter.drawImage(bufferedImage, currX, currZ, bufferedImage.getWidth() - currX, bufferedImage.getHeight() - currZ);
                    this.nmsAdapter.sendPacket(player, this.nmsAdapter.constructFramePacket(entity.getEntityId(), entity.getEntityId() + 100));
                    this.nmsAdapter.sendPacket(player, this.nmsAdapter.constructMapPacket(entity.getEntityId() + 100, (byte) 0, false, true, new ArrayList<>(), drawAdapter.toArray()));

                    if (currX + 128 >= bufferedImage.getWidth()) {
                        currZ += 128;
                        currX = 0;

                        if (currZ >= bufferedImage.getHeight()) {
                            break;
                        }
                    } else {
                        currX += 128;
                    }
                }
            } else if (args[0].equalsIgnoreCase("makemap")) {
                final ItemStack map = player.getInventory().getItemInMainHand();
                final MapMeta mapMeta = (MapMeta) map.getItemMeta();
                //mapMeta.getMapView().getRenderers().forEach(mapRenderer -> mapMeta.getMapView().removeRenderer(mapRenderer));
                final int mapId = mapMeta.getMapId();

                final DrawAdapterImpl drawAdapter = new DrawAdapterImpl();
                //this.drawAdapter.line(10, 30, 127, 127, this.nmsAdapter.matchRgb(0, 0, 0));
                drawAdapter.fill(this.nmsAdapter.matchRgb(Color.ORANGE.getRed(), Color.ORANGE.getGreen(), Color.ORANGE.getBlue()));
                drawAdapter.rect(10, 10, 20, 20, this.nmsAdapter.matchRgb(255, 255, 255));
                drawAdapter.fillRect(12, 12, 18, 18, this.nmsAdapter.matchRgb(0, 0, 0));
                drawAdapter.line(0, 100, 127, 100, this.nmsAdapter.matchRgb(255, 255, 255));

                try {
                    drawAdapter.drawImage(ImageIO.read(new URL(args[1])));
                } catch (final IOException e) {
                    e.printStackTrace();
                }

                final Object packet = this.nmsAdapter.constructMapPacket(mapId, (byte) 0, true, true, Arrays.asList(
                        new MapIcon(MapIcon.Type.BANNER_BLACK, 64, 64, 0, "Nice"),
                        new MapIcon(MapIcon.Type.MONUMENT, 64, 74, 0, ""),
                        new MapIcon(MapIcon.Type.RED_X, 10, 10, 0, null)
                ), drawAdapter.toArray());
                this.nmsAdapter.sendPacket(player, packet);
            }
        }
        return true;
    }

}

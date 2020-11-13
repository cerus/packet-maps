package de.cerus.packetmaps.plugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import de.cerus.jgif.GifImage;
import de.cerus.packetmaps.core.factory.DrawAdapterFactory;
import de.cerus.packetmaps.core.gif.task.GifRendererTask;
import de.cerus.packetmaps.core.pmcache.GifCache;
import de.cerus.packetmaps.core.pmcache.PMCacheUtil;
import de.cerus.packetmaps.core.pmcache.VideoCache;
import de.cerus.packetmaps.nmsbase.NmsAdapter;
import de.cerus.packetmaps.nmsbase.screen.MapScreen;
import de.cerus.packetmaps.plugin.util.EntityUtil;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;

@CommandAlias("packetmaps|maps")
public class PacketMapsCommand extends BaseCommand {

    private static final String FORMAT = "§6Packet Maps §8» §7%s";

    private final Map<Integer, MapScreen> temporaryScreenMap = new HashMap<>();

    @Dependency
    private JavaPlugin plugin;
    @Dependency
    private NmsAdapter nmsAdapter;
    @Dependency
    private DrawAdapterFactory drawAdapterFactory;

    @CatchUnknown
    public void handleUnknown(final Player player) {
    }

    @Subcommand("vid")
    public void handleVid(final Player player, final String path) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            final File file = new File(path);
            try {
                final FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
                final VideoCache videoCache = VideoCache.buildCache(grab, this.nmsAdapter);
                PMCacheUtil.write(new File("./" + UUID.randomUUID().toString() + ".pmcache"), videoCache);
                player.sendMessage("done");
            } catch (final IOException | JCodecException e) {
                e.printStackTrace();
            }
        });
    }

    @Subcommand("vid2")
    public void handleVid2(final Player player, final int screenId, final String path) {
        if (!PacketMapsCommand.this.temporaryScreenMap.containsKey(screenId)) {
            player.sendMessage(String.format(FORMAT, "§aScreen not found"));
            return;
        }
        final MapScreen mapScreen = PacketMapsCommand.this.temporaryScreenMap.get(screenId);
        mapScreen.addObserver(player);

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            final VideoCache cache;
            try {
                final File file = new File(path);
                player.sendMessage("reading..");
                cache = PMCacheUtil.read(file);
                player.sendMessage("resizing..");
                cache.resize(mapScreen.getTotalDimensions());
            } catch (final IOException e) {
                e.printStackTrace();
                player.sendMessage(e.getMessage());
                return;
            }

            final AtomicInteger currFrame = new AtomicInteger(0);
            final int maxFrame = cache.getFrameCount();
            new BukkitRunnable() {
                @Override
                public void run() {
                    final byte[] cachedFrame = cache.getCachedFrames()[currFrame.get()];
                    mapScreen.getScreenGraphics().setData(cachedFrame);

                    mapScreen.update();
                    mapScreen.send();

                    if (currFrame.addAndGet(1) >= maxFrame) {
                        this.cancel();
                        player.sendMessage("over");
                    }
                }
            }.runTaskTimerAsynchronously(this.plugin, 0, 1);
        });
    }

    @Subcommand("screen")
    public class ScreenCommand extends BaseCommand {

        @Subcommand("delete")
        public void handleDelete(final Player player, final int id) {
            if (!PacketMapsCommand.this.temporaryScreenMap.containsKey(id)) {
                player.sendMessage(String.format(FORMAT, "§cScreen not found"));
                return;
            }

            final MapScreen mapScreen = PacketMapsCommand.this.temporaryScreenMap.remove(id);
            mapScreen.getScreenGraphics().clearData();
            mapScreen.send();
            mapScreen.clearObservers();

            player.sendMessage(String.format(FORMAT, "Screen was removed"));
        }

        @Subcommand("create")
        public void handleCreate(final Player player) {
            final Optional<ItemFrame> optional = player.getNearbyEntities(5, 5, 5).stream()
                    .filter(entity -> entity instanceof ItemFrame)
                    .filter(entity -> EntityUtil.isLookingAt(player, entity))
                    .map(entity -> (ItemFrame) entity)
                    .findFirst();
            if (!optional.isPresent()) {
                player.sendMessage(String.format(FORMAT, "§cPlease look at an item frame."));
                return;
            }

            final ItemFrame itemFrame = optional.get();
            final EntityUtil.ItemFrameResult itemFrameResult = EntityUtil.getNearbyItemFrames(itemFrame, itemFrame.getFacing(), 10, 10);
            final int width = itemFrameResult.getWidth();
            final int height = itemFrameResult.getHeight();

            final Entity[][] entities = new Entity[width][height];
            int n = 0;
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < height; z++) {
                    entities[x][z] = itemFrameResult.getFrames().get(n++);
                }
            }
            final MapScreen mapScreen = new MapScreen(width, height, entities, PacketMapsCommand.this.nmsAdapter);

            int nextId = 0;
            for (int i = 0; i < 500; i++) {
                if (!PacketMapsCommand.this.temporaryScreenMap.containsKey(i)) {
                    nextId = i;
                    break;
                }
            }
            PacketMapsCommand.this.temporaryScreenMap.put(nextId, mapScreen);

            player.sendMessage(String.format(FORMAT, "§aCreated screen #" + nextId + " (" + width + "*" + height + ")"));
        }

        @Subcommand("cachegif")
        public class CacheGifCommand extends BaseCommand {

            @Subcommand("file")
            public void handleFile(final Player player, final String gifPath) {
                final File file = new File(gifPath);
                final GifImage gifImage = new GifImage();

                PacketMapsCommand.this.plugin.getServer().getScheduler().runTaskAsynchronously(PacketMapsCommand.this.plugin, () -> {
                    final GifCache cache;
                    try {
                        gifImage.loadFrom(file);
                        cache = GifCache.buildCache(gifImage, new Dimension(gifImage.getFirstFrame().getWidth(),
                                gifImage.getFirstFrame().getHeight()), PacketMapsCommand.this.nmsAdapter);

                        PMCacheUtil.write(file.getParentFile().getAbsolutePath(), file.getName(), cache);
                    } catch (final IOException | IllegalStateException e) {
                        player.sendMessage(String.format(FORMAT, "§cError: " + e.getMessage()));
                        return;
                    }

                    player.sendMessage(String.format(FORMAT, "§aGIF was cached §7(" + new File(file.getParentFile().getAbsolutePath(),
                            file.getName() + ".pmcache").getAbsolutePath() + ")"));
                });
            }

            @Subcommand("url")
            public void handleUrl(final Player player, final String url) {
                final File outFile = new File("./" + url.replaceAll("[;,:.\\-_$§%&/()\\\\!?\\[]\\+#]", "") + ".pmcache");
                final GifImage gifImage = new GifImage();

                PacketMapsCommand.this.plugin.getServer().getScheduler().runTaskAsynchronously(PacketMapsCommand.this.plugin, () -> {
                    final GifCache cache;
                    try {
                        final URLConnection urlConnection = new URL(url).openConnection();
                        gifImage.loadFrom(urlConnection.getInputStream());
                        cache = GifCache.buildCache(gifImage, new Dimension(gifImage.getFirstFrame().getWidth(),
                                gifImage.getFirstFrame().getHeight()), PacketMapsCommand.this.nmsAdapter);

                        PMCacheUtil.write(outFile, cache);
                    } catch (final IOException | IllegalStateException e) {
                        player.sendMessage(String.format(FORMAT, "§cError: " + e.getMessage()));
                        return;
                    }

                    player.sendMessage(String.format(FORMAT, "§aGIF was cached §7(" + outFile.getAbsolutePath() + ")"));
                });
            }

        }

        @Subcommand("playgif")
        public class PlayGifCommand extends BaseCommand {

            @Subcommand("file")
            public void handleFile(final Player player, final int screenId, final String path) {
                final GifCache cache;
                try {
                    cache = PMCacheUtil.read(new File(path));
                } catch (final IOException | IllegalStateException e) {
                    player.sendMessage(String.format(FORMAT, "§cError: " + e.getMessage()));
                    e.printStackTrace();
                    return;
                }

                this.handle(player, screenId, cache);
            }

            @Subcommand("url")
            public void handleUrl(final Player player, final int screenId, final String url) {
                final GifCache cache;
                try {
                    cache = PMCacheUtil.read(new URL(url));
                } catch (final IOException | IllegalStateException e) {
                    player.sendMessage(String.format(FORMAT, "§cError: " + e.getMessage()));
                    e.printStackTrace();
                    return;
                }

                this.handle(player, screenId, cache);
            }

            private void handle(final Player player, final int screenId, final GifCache cache) {
                if (!PacketMapsCommand.this.temporaryScreenMap.containsKey(screenId)) {
                    player.sendMessage(String.format(FORMAT, "§aScreen not found"));
                    return;
                }
                final MapScreen mapScreen = PacketMapsCommand.this.temporaryScreenMap.get(screenId);
                mapScreen.addObserver(player);

                if (cache == null) {
                    player.sendMessage(String.format(FORMAT, "§cError"));
                    return;
                }

                player.sendMessage(String.format(FORMAT, "Starting playback..."));

                cache.resize(mapScreen.getTotalDimensions());
                final GifRendererTask gifRendererTask = new GifRendererTask(mapScreen, cache);

                final AtomicInteger count = new AtomicInteger(0);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        gifRendererTask.run();

                        if (count.incrementAndGet() >= 500) {
                            this.cancel();
                            player.sendMessage(String.format(FORMAT, "Done"));
                        }
                    }
                }.runTaskTimerAsynchronously(PacketMapsCommand.this.plugin, 0, 1);
            }

        }

    }

}

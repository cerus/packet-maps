package de.cerus.packetmaps.v1_16_3;

import de.cerus.packetmaps.nmsbase.MapIcon;
import de.cerus.packetmaps.nmsbase.NmsAdapter;
import de.cerus.packetmaps.nmsbase.util.ReflectionUtil;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import net.minecraft.server.v1_16_R2.ChatComponentText;
import net.minecraft.server.v1_16_R2.DataWatcher;
import net.minecraft.server.v1_16_R2.DataWatcherObject;
import net.minecraft.server.v1_16_R2.DataWatcherRegistry;
import net.minecraft.server.v1_16_R2.ItemStack;
import net.minecraft.server.v1_16_R2.Packet;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R2.PacketPlayOutMap;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.MapMeta;

public class NmsAdapterImpl implements NmsAdapter {

    @Override
    public void sendPacket(final Player player, final Object packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket((Packet<?>) packet);
    }

    @Override
    public Object constructMapPacket(final int mapId,
                                     final byte scale,
                                     final boolean trackPos,
                                     final boolean locked,
                                     final Collection<MapIcon> icons,
                                     final byte[][] data) {
        final byte[] arr = new byte[128 * 128];
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                arr[x + z * 128] = data[x][z];
            }
        }
        return this.constructMapPacket(mapId, scale, trackPos, locked, icons, arr, 0, 0, 128, 128);
    }

    @Override
    public Object constructMapPacket(final int mapId,
                                     final byte scale,
                                     final boolean trackPos,
                                     final boolean locked,
                                     final Collection<MapIcon> icons,
                                     final byte[] data,
                                     final int minX,
                                     final int minZ,
                                     final int width,
                                     final int height) {
        return new PacketPlayOutMap(mapId, scale, trackPos, locked, icons.stream()
                .map(mapIcon -> new net.minecraft.server.v1_16_R2.MapIcon(
                        net.minecraft.server.v1_16_R2.MapIcon.Type.valueOf(mapIcon.getType().name()),
                        (byte) mapIcon.getX(),
                        (byte) mapIcon.getY(),
                        (byte) mapIcon.getRotation(),
                        mapIcon.getName() == null ? null : new ChatComponentText(mapIcon.getName())
                )).collect(Collectors.toList()), data, minX, minZ, width, height);

    }

    @Override
    public Object constructFramePacket(final int frameId, final int mapId) {
        final PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata();
        ReflectionUtil.setField(packet, packet.getClass(), "a", frameId);

        final org.bukkit.inventory.ItemStack mapItem = new org.bukkit.inventory.ItemStack(Material.FILLED_MAP, 1);
        final MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
        mapMeta.setMapId(mapId);
        mapItem.setItemMeta(mapMeta);

        final DataWatcherObject<ItemStack> itemObj = DataWatcherRegistry.g.a((byte) 7);
        final DataWatcher.Item<ItemStack> itemItem = new DataWatcher.Item<>(itemObj, CraftItemStack.asNMSCopy(mapItem));
        ReflectionUtil.setField(packet, packet.getClass(), "b", Collections.singletonList(itemItem));

        return packet;
    }

    @Override
    public byte matchRgb(final int r, final int g, final int b) {
        return (byte) this.matchRgbPalette(r, g, b).getId();
    }

    @Override
    public Color matchColor(final byte color) {
        final MapPalette mapPalette = MapPalette.fromId(color);
        return mapPalette == null ? MapPalette.TRANSPARENT_2.getColor() : mapPalette.getColor();
    }

    public MapPalette matchRgbPalette(final int r, final int g, final int b) {
        return Arrays.stream(MapPalette.values())
                .filter(mapPalette -> mapPalette.getId() > 3)
                .min(Comparator.comparingDouble(value -> this.calcDist(value, r, g, b)))
                .orElse(MapPalette.TRANSPARENT_0);
    }

    private double calcDist(final MapPalette mapPalette, final int r, final int g, final int b) {
        final Color color = mapPalette.getColor();
        return Math.sqrt(Math.pow(r - color.getRed(), 2) + Math.pow(g - color.getGreen(), 2) + Math.pow(b - color.getBlue(), 2));
    }

}

package de.cerus.packetmaps.nmsbase;

import java.awt.Color;
import java.util.Collection;
import org.bukkit.entity.Player;

public interface NmsAdapter {

    void sendPacket(Player player, Object packet);

    Object constructMapPacket(int mapId, byte scale, boolean trackPos, boolean locked, Collection<MapIcon> icons, byte[][] data);

    Object constructMapPacket(int mapId, byte scale, boolean trackPos, boolean locked, Collection<MapIcon> icons, byte[] data, int minX, int minZ, int width, int height);

    Object constructFramePacket(int frameId, int mapId);

    byte matchRgb(int r, int g, int b);

    Color matchColor(byte color);

}

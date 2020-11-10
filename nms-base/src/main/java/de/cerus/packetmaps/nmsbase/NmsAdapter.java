package de.cerus.packetmaps.nmsbase;

import java.util.Collection;
import org.bukkit.entity.Player;

public interface NmsAdapter {

    void sendPacket(Player player, Object packet);

    Object constructMapPacket(int mapId, byte scale, boolean trackPos, boolean locked, Collection<MapIcon> icons, byte[][] data);

    byte matchRgb(int r, int g, int b);

}

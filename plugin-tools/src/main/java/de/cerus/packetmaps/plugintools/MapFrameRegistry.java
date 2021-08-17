package de.cerus.packetmaps.plugintools;

import de.cerus.packetmaps.nmsbase.FakeMap;
import java.util.Map;
import org.bukkit.entity.ItemFrame;

public interface MapFrameRegistry {

    Map<ItemFrame, FakeMap> getMapFrameMap();

}

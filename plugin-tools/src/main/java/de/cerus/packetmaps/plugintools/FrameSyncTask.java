package de.cerus.packetmaps.plugintools;

import de.cerus.packetmaps.nmsbase.FakeMap;
import de.cerus.packetmaps.nmsbase.NmsAdapter;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

public class FrameSyncTask implements Runnable {

    private final MapFrameRegistry mapFrameRegistry;
    private final NmsAdapter nmsAdapter;

    public FrameSyncTask(final MapFrameRegistry mapFrameRegistry, final NmsAdapter nmsAdapter) {
        this.mapFrameRegistry = mapFrameRegistry;
        this.nmsAdapter = nmsAdapter;
    }

    @Override
    public void run() {
        final Map<ItemFrame, FakeMap> map = this.mapFrameRegistry.getMapFrameMap();
        for (final Map.Entry<ItemFrame, FakeMap> entry : map.entrySet()) {
            final ItemFrame itemFrame = entry.getKey();
            final Location frameLocation = itemFrame.getLocation();
            final FakeMap fakeMap = entry.getValue();

            for (final Player observer : fakeMap.getObservers()) {
                if (observer.getLocation().distanceSquared(frameLocation) < 30 * 30) {
                    this.nmsAdapter.sendPacket(observer, this.nmsAdapter.constructFramePacket(itemFrame.getEntityId(), fakeMap.getId()));
                }
            }
        }
    }

}

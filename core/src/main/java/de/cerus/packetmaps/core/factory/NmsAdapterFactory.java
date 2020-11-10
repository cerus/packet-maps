package de.cerus.packetmaps.core.factory;

import de.cerus.packetmaps.nmsbase.NmsAdapter;
import org.bukkit.Bukkit;

public class NmsAdapterFactory {

    public NmsAdapter makeAdapter() {
        String version = Bukkit.getVersion();
        version = version.substring(version.indexOf("MC: ") + 4, version.lastIndexOf('.'));

        switch (version) {
            case "1.16":
                return new de.cerus.packetmaps.v1_16_3.NmsAdapterImpl();
            default:
                throw new IllegalStateException("Invalid server version '" + version + "'");
        }
    }

}

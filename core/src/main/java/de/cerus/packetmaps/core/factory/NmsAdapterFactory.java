package de.cerus.packetmaps.core.factory;

import de.cerus.packetmaps.nmsbase.NmsAdapter;
import org.bukkit.Bukkit;

public class NmsAdapterFactory {

    public NmsAdapter makeAdapter() {
        String version = Bukkit.getVersion();
        version = version.substring(version.indexOf("MC: ") + 4, version.lastIndexOf(')'));

        switch (version) {
            case "1.16.3":
                return new de.cerus.packetmaps.v1_16_3.NmsAdapterImpl();
            case "1.16.4":
            case "1.16.5":
                return new de.cerus.packetmaps.v1_16_4.NmsAdapterImpl();
            default:
                throw new IllegalStateException("Invalid server version '" + version + "'");
        }
    }

}

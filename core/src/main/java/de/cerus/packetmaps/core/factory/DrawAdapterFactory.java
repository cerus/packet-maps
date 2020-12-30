package de.cerus.packetmaps.core.factory;

import de.cerus.packetmaps.nmsbase.DrawAdapter;
import org.bukkit.Bukkit;

public class DrawAdapterFactory {

    public DrawAdapter makeAdapter() {
        String version = Bukkit.getVersion();
        version = version.substring(version.indexOf("MC: ") + 4, version.lastIndexOf(')'));

        switch (version) {
            case "1.16.3":
                return new de.cerus.packetmaps.v1_16_3.DrawAdapterImpl();
            case "1.16.4":
                return new de.cerus.packetmaps.v1_16_4.DrawAdapterImpl();
            default:
                throw new IllegalStateException("Invalid server version '" + version + "'");
        }
    }

}

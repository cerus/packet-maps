package de.cerus.packetmaps.core;

import de.cerus.packetmaps.core.command.TestCommand;
import de.cerus.packetmaps.core.factory.DrawAdapterFactory;
import de.cerus.packetmaps.core.factory.NmsAdapterFactory;
import de.cerus.packetmaps.nmsbase.DrawAdapter;
import de.cerus.packetmaps.nmsbase.NmsAdapter;
import org.bukkit.plugin.java.JavaPlugin;

public class PacketMapsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        final NmsAdapter nmsAdapter = new NmsAdapterFactory().makeAdapter();
        final DrawAdapter drawAdapter = new DrawAdapterFactory().makeAdapter();

        this.getCommand("test").setExecutor(new TestCommand(drawAdapter, nmsAdapter));
    }

}

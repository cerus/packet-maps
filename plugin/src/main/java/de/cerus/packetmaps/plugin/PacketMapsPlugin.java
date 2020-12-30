package de.cerus.packetmaps.plugin;

import co.aikar.commands.BukkitCommandManager;
import de.cerus.packetmaps.core.factory.DrawAdapterFactory;
import de.cerus.packetmaps.core.factory.NmsAdapterFactory;
import de.cerus.packetmaps.nmsbase.NmsAdapter;
import de.cerus.packetmaps.plugin.commands.PacketMapsCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class PacketMapsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        final NmsAdapterFactory nmsAdapterFactory = new NmsAdapterFactory();
        final NmsAdapter nmsAdapter = nmsAdapterFactory.makeAdapter();
        final DrawAdapterFactory drawAdapterFactory = new DrawAdapterFactory();

        this.getLogger().info("Using nms '" + nmsAdapter.getClass().getName()
                .replace("de.cerus.packetmaps.", "").split("\\.")[0] + "'");

        final BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.registerDependency(NmsAdapter.class, nmsAdapter);
        commandManager.registerDependency(DrawAdapterFactory.class, drawAdapterFactory);
        commandManager.registerCommand(new PacketMapsCommand());
    }

}

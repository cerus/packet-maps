package de.cerus.packetmaps.core.command;

import de.cerus.packetmaps.nmsbase.DrawAdapter;
import de.cerus.packetmaps.nmsbase.NmsAdapter;
import java.util.ArrayList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

public class TestCommand implements CommandExecutor {

    private final DrawAdapter drawAdapter;
    private final NmsAdapter nmsAdapter;

    public TestCommand(final DrawAdapter drawAdapter, final NmsAdapter nmsAdapter) {
        this.drawAdapter = drawAdapter;
        this.nmsAdapter = nmsAdapter;
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        if (!(commandSender instanceof Player)) {
            return true;
        }
        final Player player = (Player) commandSender;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("makemap")) {
                final ItemStack map = player.getInventory().getItemInMainHand();
                final MapMeta mapMeta = (MapMeta) map.getItemMeta();
                mapMeta.getMapView().getRenderers().forEach(mapRenderer -> mapMeta.getMapView().removeRenderer(mapRenderer));
                final int mapId = mapMeta.getMapId();

                player.getInventory().setItemInMainHand(map);

                this.drawAdapter.line(10, 30, 127, 127, this.nmsAdapter.matchRgb(0, 0, 0));
                final Object packet = this.nmsAdapter.constructMapPacket(mapId, (byte) 0, false, false, new ArrayList<>(), this.drawAdapter.toArray());
                this.nmsAdapter.sendPacket(player, packet);
            }
        }
        return true;
    }

}

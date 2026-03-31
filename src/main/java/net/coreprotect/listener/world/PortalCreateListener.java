package net.coreprotect.listener.world;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.Lookup;
import net.coreprotect.utility.BlockUtils;

public final class PortalCreateListener extends Queue implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onPortalCreate(PortalCreateEvent event) {
        World world = event.getWorld();
        if (event.isCancelled() || !Config.getConfig(world).PORTALS) {
            return;
        }

        String user = "#portal";
        for (Block block : event.getBlocks()) {
            Material type = block.getType();
            if (type == Material.PORTAL || type == Material.FIRE) {
                String resultData = Lookup.whoPlacedCache(block.getState());
                if (resultData.length() > 0) {
                    user = resultData;
                    break;
                }
            }
        }

        for (Block block : event.getBlocks()) {
            BlockState blockState = block.getState();
            Material type = blockState.getType();
            if (BlockUtils.isAir(type)) {
                Queue.queueBlockBreak(user, blockState, type, null, 0);
            }
            else {
                Queue.queueBlockPlace(user, blockState, type, blockState, type, -1, 0, null);
            }
        }
    }
}

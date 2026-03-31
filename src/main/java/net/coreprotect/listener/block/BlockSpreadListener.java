package net.coreprotect.listener.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;

import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.thread.CacheHandler;
import net.coreprotect.utility.Util;
import net.coreprotect.utility.WorldUtils;

public final class BlockSpreadListener extends Queue implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onBlockSpread(BlockSpreadEvent event) {
        // mushrooms, fire

        /* To-do: Improve configuration
         *
         * # Track when a block changes states, such as from natural block growth.
         * block-change: true
         *
         */
        if (event.isCancelled()) {
            return;
        }

        BlockState blockstate = event.getNewState();
        Material type = blockstate.getType();

        if (Config.getConfig(event.getBlock().getWorld()).VINE_GROWTH && (BlockGroup.VINES.contains(type) || BlockGroup.AMETHYST.contains(type))) {
            Block block = event.getBlock();
            if (checkCacheData(block, type)) {
                return;
            }

            if (BlockGroup.VINES.contains(type)) {
                queueBlockPlace("#vine", block.getState(), block.getType(), null, type, -1, 0, null);
            }
            else if (BlockGroup.AMETHYST.contains(type)) {
                queueBlockPlace("#amethyst", block.getState(), block.getType(), block.getState(), type, -1, 0, null);
            }
        }
    }

    private boolean checkCacheData(Block block, Material type) {
        String cacheId = block.getX() + "." + block.getY() + "." + block.getZ() + "." + WorldUtils.getWorldId(block.getWorld().getName());
        Location location = block.getLocation();
        int timestamp = (int) (System.currentTimeMillis() / 1000L);
        Object[] cacheData = CacheHandler.spreadCache.get(cacheId);
        CacheHandler.spreadCache.put(cacheId, new Object[] { timestamp, type });
        if (cacheData != null && ((Material) cacheData[1]) == type) {
            return true;
        }

        return false;
    }
}

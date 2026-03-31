package net.coreprotect.listener.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.Lookup;
import net.coreprotect.thread.CacheHandler;
import net.coreprotect.utility.WorldUtils;

public final class BlockFromToListener extends Queue implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    protected void onBlockFromTo(BlockFromToEvent event) {
        Block block = event.getBlock();
        Material type = block.getType(); // old block type
        if (!event.isCancelled()) {
            World world = event.getBlock().getWorld();
            if ((Config.getConfig(world).WATER_FLOW && type.equals(Material.WATER)) || (Config.getConfig(world).WATER_FLOW && type.equals(Material.STATIONARY_WATER)) || (Config.getConfig(world).LAVA_FLOW && type.equals(Material.LAVA)) || (Config.getConfig(world).LAVA_FLOW && type.equals(Material.STATIONARY_LAVA))) {
                if (type == Material.STATIONARY_WATER) {
                    type = Material.WATER;
                }
                else if (type == Material.STATIONARY_LAVA) {
                    type = Material.LAVA;
                }

                Block toBlock = event.getToBlock();
                BlockState toBlockState = toBlock.getState();

                byte waterLevel = block.getData();
                int level = waterLevel + 1;
                if (level > 8) {
                    level = level - 8;
                }

                if (toBlock.isEmpty()) {
                    toBlockState = null;
                }

                String f = "#flow";
                if (type.equals(Material.WATER)) {
                    f = "#water";
                }
                else if (type.equals(Material.LAVA)) {
                    f = "#lava";
                }

                int unixtimestamp = (int) (System.currentTimeMillis() / 1000L);
                int x = toBlock.getX();
                int y = toBlock.getY();
                int z = toBlock.getZ();
                int wid = WorldUtils.getWorldId(block.getWorld().getName());
                if (Config.getConfig(world).LIQUID_TRACKING) {
                    String p = Lookup.whoPlacedCache(block);
                    if (p.length() > 0) {
                        f = p;
                    }
                }

                if (f.startsWith("#")) {
                    String cacheId = toBlock.getX() + "." + toBlock.getY() + "." + toBlock.getZ() + "." + WorldUtils.getWorldId(toBlock.getWorld().getName());
                    int timestamp = (int) (System.currentTimeMillis() / 1000L);
                    Object[] cacheData = CacheHandler.spreadCache.get(cacheId);
                    CacheHandler.spreadCache.put(cacheId, new Object[] { timestamp, type });
                    if (toBlockState == null && cacheData != null && ((Material) cacheData[1]) == type) {
                        return;
                    }
                }

                CacheHandler.lookupCache.put("" + x + "." + y + "." + z + "." + wid + "", new Object[] { unixtimestamp, f, type });
                Queue.queueBlockPlace(f, toBlock.getState(), block.getType(), toBlockState, type, -1, 0, null);
            }
            else if (type.equals(Material.DRAGON_EGG)) {
                Location location = block.getLocation();
                int worldId = WorldUtils.getWorldId(location.getWorld().getName());
                int x = location.getBlockX();
                int y = location.getBlockY();
                int z = location.getBlockZ();
                String coordinates = x + "." + y + "." + z + "." + worldId + "." + type.name();
                String user = "#entity";

                Object[] data = CacheHandler.interactCache.get(coordinates);
                if (data != null && data[1] == Material.DRAGON_EGG) {
                    long newTime = System.currentTimeMillis();
                    long oldTime = (long) data[0];

                    if ((newTime - oldTime) < 20) { // 50ms = 1 tick
                        user = (String) data[2];
                    }
                    CacheHandler.interactCache.remove(coordinates);
                }

                if (Config.getConfig(block.getWorld()).BLOCK_BREAK) {
                    Queue.queueBlockBreak(user, block.getState(), block.getType(), null, 0);
                }
                if (Config.getConfig(block.getWorld()).BLOCK_PLACE) {
                    Block toBlock = event.getToBlock();
                    BlockState toBlockState = toBlock.getState();
                    if (Config.getConfig(world).BLOCK_MOVEMENT) {
                        toBlockState = BlockUtil.gravityScan(toBlock.getLocation(), type, user).getState();
                    }

                    Queue.queueBlockPlace(user, toBlockState, block.getType(), toBlockState, type, -1, 0, null);
                }
            }
        }
    }

}

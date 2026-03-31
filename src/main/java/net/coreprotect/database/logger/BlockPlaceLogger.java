package net.coreprotect.database.logger;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import net.coreprotect.CoreProtect;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.statement.BlockStatement;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.database.statement.UserStatement;
import net.coreprotect.event.CoreProtectPreLogEvent;
import net.coreprotect.thread.CacheHandler;
import net.coreprotect.utility.MaterialUtils;
import net.coreprotect.utility.WorldUtils;

public class BlockPlaceLogger {

    private BlockPlaceLogger() {
        throw new IllegalStateException("Database class");
    }

    public static void log(PreparedStatement preparedStmt, int batchCount, String user, BlockState block, int replacedType, int replacedData, Material forceType, int forceData, boolean force, List<Object> meta, String blockData, String replaceBlockData) {
        try {
            if (user == null || ConfigHandler.blacklist.get(user.toLowerCase(Locale.ROOT)) != null) {
                return;
            }

            Material type = block.getType();
            int data = block.getRawData();
            if (forceType != null && force) {
                type = forceType;
                if (BukkitAdapter.ADAPTER.isItemFrame(type) || type.equals(Material.MOB_SPAWNER) || type.equals(Material.PAINTING) || type.equals(Material.SKULL) || type.equals(Material.ARMOR_STAND)) {
                    data = forceData;
                }
                else if (user.startsWith("#")) {
                    data = forceData;
                }
            }
            else if (forceType != null && !type.equals(forceType)) {
                type = forceType;
                data = forceData;
            }

            if (type.equals(Material.AIR)) {
                return;
            }

            if (ConfigHandler.blacklist.get("minecraft:" + type.name().toLowerCase(Locale.ROOT)) != null) {
                return;
            }

            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            long chunkKey = (x >> 4) & 0xffffffffL | ((z >> 4) & 0xffffffffL) << 32;
            if (ConfigHandler.populatedChunks.get(chunkKey) != null) {
                boolean isWater = user.equals("#water");
                boolean isLava = user.equals("#lava");
                boolean isVine = user.equals("#vine");
                if (isWater || isLava || isVine) {
                    int timeDelay = isWater ? 60 : 240;
                    long timeSincePopulation = ((System.currentTimeMillis() / 1000L) - ConfigHandler.populatedChunks.getOrDefault(chunkKey, 0L));
                    if (timeSincePopulation <= timeDelay) {
                        return;
                    }

                    if (timeSincePopulation > 240) {
                        ConfigHandler.populatedChunks.remove(chunkKey);
                    }
                }
                else if (type == Material.WATER || type == Material.LAVA) {
                    ConfigHandler.populatedChunks.remove(chunkKey);
                }
            }

            CoreProtectPreLogEvent event = new CoreProtectPreLogEvent(user, block.getLocation());
            if (Config.getGlobal().API_ENABLED && !Bukkit.isPrimaryThread()) {
                CoreProtect.getInstance().getServer().getPluginManager().callEvent(event);
            }

            int userId = UserStatement.getId(preparedStmt, event.getUser(), true);
            Location eventLocation = event.getLocation();
            int wid = WorldUtils.getWorldId(eventLocation.getWorld().getName());
            int time = (int) (System.currentTimeMillis() / 1000L);

            // Use event location for subsequent logging
            x = eventLocation.getBlockX();
            y = eventLocation.getBlockY();
            z = eventLocation.getBlockZ();

            if (event.getUser().length() > 0) {
                CacheHandler.lookupCache.put("" + x + "." + y + "." + z + "." + wid + "", new Object[] { time, event.getUser(), type });
            }

            if (event.isCancelled()) {
                return;
            }

            int internalType = MaterialUtils.getBlockId(type.name(), true);
            if (replacedType > 0 && MaterialUtils.getType(replacedType) != Material.AIR) {
                BlockStatement.insert(preparedStmt, batchCount, time, userId, wid, x, y, z, replacedType, replacedData, null, replaceBlockData, 0, 0);
            }

            BlockStatement.insert(preparedStmt, batchCount, time, userId, wid, x, y, z, internalType, data, meta, blockData, 1, 0);

            // Log second half of double-height blocks (doors, beds)
            if (type.equals(Material.IRON_DOOR_BLOCK) || BlockGroup.DOORS.contains(type)) {
                if (data < 8) { // bottom half only
                    int topData = data | 0x8;
                    try {
                        Block topBlock = block.getWorld().getBlockAt(x, y + 1, z);
                        Material topType = topBlock.getType();
                        if (BlockGroup.DOORS.contains(topType) || topType == Material.IRON_DOOR_BLOCK) {
                            topData = topBlock.getData();
                        }
                    }
                    catch (Exception ignored) {
                    }
                    BlockStatement.insert(preparedStmt, batchCount, time, userId, wid, x, y + 1, z, internalType, topData, null, null, 1, 0);
                }
            }
            else if (type.equals(Material.BED_BLOCK) && (data & 0x8) == 0) { // foot part only
                int dx = x, dz = z;
                int facing = data & 0x3;
                if (facing == 0) dz = z + 1;
                else if (facing == 1) dx = x - 1;
                else if (facing == 2) dz = z - 1;
                else if (facing == 3) dx = x + 1;
                BlockStatement.insert(preparedStmt, batchCount, time, userId, wid, dx, y, dz, internalType, data + 8, null, null, 1, 0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}

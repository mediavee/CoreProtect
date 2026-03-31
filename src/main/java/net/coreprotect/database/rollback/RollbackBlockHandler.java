package net.coreprotect.database.rollback;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.paper.PaperAdapter;
import net.coreprotect.thread.CacheHandler;
import net.coreprotect.utility.BlockUtils;
import net.coreprotect.utility.ChestTool;
import net.coreprotect.utility.EntityUtils;
import net.coreprotect.utility.ItemUtils;
import net.coreprotect.utility.Util;
import net.coreprotect.utility.entity.HangingUtil;

public class RollbackBlockHandler extends Queue {

    @SuppressWarnings("deprecation")
    public static boolean processBlockChange(World bukkitWorld, Block block, Object[] row, int rollbackType, boolean clearInventories, Map<Block, byte[]> chunkChanges, boolean countBlock, Material oldTypeMaterial, Material pendingChangeType, byte pendingChangeData, String finalUserString, byte rawBlockData, Material changeType, boolean changeBlock, byte changeBlockData, ArrayList<Object> meta, byte blockData, String rowUser, Material rowType, int rowX, int rowY, int rowZ, int rowTypeRaw, int rowData, int rowAction, int rowWorldId, String blockDataString) {
        int unixtimestamp = (int) (System.currentTimeMillis() / 1000L);

        try {
            if (changeBlock) {
                /* In 1.8, piston head / technical piston handling is simplified: just set the block type + data directly */
                if (rowType == Material.PISTON_EXTENSION) {
                    // Piston head being modified - retract the base piston
                    int facing = changeBlockData & 0x7;
                    Block pistonBlock = getPistonBase(block, facing);
                    if (pistonBlock != null) {
                        byte pistonData = pistonBlock.getData();
                        Material pistonType = pistonBlock.getType();
                        if (pistonType == Material.PISTON_BASE || pistonType == Material.PISTON_STICKY_BASE) {
                            // Clear extended bit
                            pistonBlock.setTypeIdAndData(pistonType.getId(), (byte) (pistonData & 0x7), false);
                        }
                    }
                }
                else if (rowType == Material.PISTON_MOVING_PIECE) {
                    // Moving piston - determine if sticky or regular
                    boolean sticky = (blockData & 0x8) != 0;
                    rowType = sticky ? Material.PISTON_STICKY_BASE : Material.PISTON_BASE;
                    blockData = (byte) (blockData & 0x7); // keep facing only
                }

                if ((rowType == Material.AIR) && ((BukkitAdapter.ADAPTER.isItemFrame(oldTypeMaterial)) || (oldTypeMaterial == Material.PAINTING))) {
                    HangingUtil.removeHanging(block.getState(), blockDataString);
                }
                else if ((BukkitAdapter.ADAPTER.isItemFrame(rowType)) || (rowType == Material.PAINTING)) {
                    HangingUtil.spawnHanging(block.getState(), rowType, blockDataString, rowData);
                }
                else if ((rowType == Material.ARMOR_STAND)) {
                    Location location1 = block.getLocation();
                    location1.setX(location1.getX() + 0.50);
                    location1.setZ(location1.getZ() + 0.50);
                    location1.setYaw(rowData);
                    boolean exists = false;

                    for (Entity entity : block.getChunk().getEntities()) {
                        if (entity instanceof ArmorStand) {
                            if (entity.getLocation().getBlockX() == location1.getBlockX() && entity.getLocation().getBlockY() == location1.getBlockY() && entity.getLocation().getBlockZ() == location1.getBlockZ()) {
                                exists = true;
                            }
                        }
                    }

                    if (!exists) {
                        Entity entity = block.getLocation().getWorld().spawnEntity(location1, EntityType.ARMOR_STAND);
                        PaperAdapter.ADAPTER.teleportAsync(entity, location1);
                    }
                }
                // END_CRYSTAL handling removed (not available in 1.8)
                else if ((rowType == Material.AIR) && ((oldTypeMaterial == Material.WATER))) {
                    // In 1.8, no waterlogged blocks exist - just set to air
                    BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, true);
                    return countBlock;
                }
                else if ((rowType == Material.AIR) && ((oldTypeMaterial == Material.SNOW))) {
                    BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, true);
                    return countBlock;
                }
                // END_CRYSTAL removal handled above (not available in 1.8)
                else if (rollbackType == 0 && rowAction == 0 && (rowType == Material.AIR)) {
                    // broke block ID #0
                }
                else if ((rowType == Material.AIR) || (rowType == Material.TNT)) {
                    if (clearInventories) {
                        if (BlockGroup.CONTAINERS.contains(changeType)) {
                            Inventory inventory = BlockUtils.getContainerInventory(block.getState(), false);
                            if (inventory != null) {
                                inventory.clear();
                            }
                        }
                        else if (BlockGroup.CONTAINERS.contains(Material.ARMOR_STAND)) {
                            if ((oldTypeMaterial == Material.ARMOR_STAND)) {
                                for (Entity entity : block.getChunk().getEntities()) {
                                    if (entity instanceof ArmorStand) {
                                        Location entityLocation = entity.getLocation();
                                        entityLocation.setY(entityLocation.getY() + 0.99);

                                        if (entityLocation.getBlockX() == rowX && entityLocation.getBlockY() == rowY && entityLocation.getBlockZ() == rowZ) {
                                            ItemUtils.getEntityEquipment((ArmorStand) entity).clear();

                                            entityLocation.setY(entityLocation.getY() - 1.99);
                                            PaperAdapter.ADAPTER.teleportAsync(entity, entityLocation);
                                            entity.remove();
                                        }
                                    }
                                }
                            }
                        }
                    }

                    boolean remove = true;
                    // In 1.8, no waterlogged blocks - simplified logic

                    if (remove) {
                        boolean physics = true;

                        // Handle bisected blocks (doors, tall plants) - in 1.8, check if it's a door or double plant
                        if (BlockGroup.DOORS.contains(changeType) || changeType == Material.IRON_DOOR_BLOCK) {
                            // Door: remove both halves
                            boolean isTopHalf = (changeBlockData & 0x8) != 0;
                            Location bisectLocation = block.getLocation().clone();
                            if (isTopHalf) {
                                bisectLocation.setY(bisectLocation.getY() - 1);
                            }
                            else {
                                bisectLocation.setY(bisectLocation.getY() + 1);
                            }

                            int worldMaxHeight = bukkitWorld.getMaxHeight();
                            if (bisectLocation.getBlockY() >= 0 && bisectLocation.getBlockY() < worldMaxHeight) {
                                Block bisectBlock = block.getWorld().getBlockAt(bisectLocation);
                                BlockUtils.prepareTypeAndData(chunkChanges, bisectBlock, rowType, (byte) 0, false);

                                if (countBlock) {
                                    updateBlockCount(finalUserString, 1);
                                }
                            }
                        }
                        else if (changeType == Material.DOUBLE_PLANT) {
                            // Double plant: top half has data & 0x8 set
                            boolean isTopHalf = (changeBlockData & 0x8) != 0;
                            Location bisectLocation = block.getLocation().clone();
                            if (isTopHalf) {
                                bisectLocation.setY(bisectLocation.getY() - 1);
                            }
                            else {
                                bisectLocation.setY(bisectLocation.getY() + 1);
                            }

                            int worldMaxHeight = bukkitWorld.getMaxHeight();
                            if (bisectLocation.getBlockY() >= 0 && bisectLocation.getBlockY() < worldMaxHeight) {
                                Block bisectBlock = block.getWorld().getBlockAt(bisectLocation);
                                BlockUtils.prepareTypeAndData(chunkChanges, bisectBlock, rowType, (byte) 0, false);

                                if (countBlock) {
                                    updateBlockCount(finalUserString, 1);
                                }
                            }
                        }
                        else if (changeType.name().endsWith("_BED") || changeType == Material.BED_BLOCK) {
                            // Bed: foot part (data & 0x8 == 0), get facing to find other half
                            if ((changeBlockData & 0x8) == 0) {
                                // This is the foot part, find head
                                Block adjacentBlock = getBedHead(block, changeBlockData);
                                if (adjacentBlock != null) {
                                    BlockUtils.prepareTypeAndData(chunkChanges, adjacentBlock, rowType, (byte) 0, false);
                                }
                            }
                        }

                        BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, (byte) 0, physics);
                    }

                    return countBlock;
                }
                else if ((rowType == Material.MOB_SPAWNER)) {
                    try {
                        BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, (byte) 0, false);
                        CreatureSpawner mobSpawner = (CreatureSpawner) block.getState();
                        mobSpawner.setSpawnedType(EntityUtils.getSpawnerType(rowData));
                        mobSpawner.update();

                        return countBlock;
                    }
                    catch (Exception e) {
                        // e.printStackTrace();
                    }
                }
                else if (rowType == Material.SKULL) { // skull
                    byte skullRotation = 0;
                    if (blockDataString != null && !blockDataString.isEmpty()) {
                        try {
                            skullRotation = Byte.parseByte(blockDataString);
                        }
                        catch (NumberFormatException e) {
                            // Ignore
                        }
                    }
                    BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, skullRotation, false);
                    if (rowData > 0) {
                        Queue.queueSkullUpdate(rowUser, block.getState(), rowData);
                    }

                    return countBlock;
                }
                else if (BukkitAdapter.ADAPTER.isSign(rowType)) {// sign
                    BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, false);
                    Queue.queueSignUpdate(rowUser, block.getState(), rollbackType, (Integer) row[1]);

                    return countBlock;
                }
                else if (BlockGroup.SHULKER_BOXES.contains(rowType)) {
                    BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, false);
                    if (countBlock) {
                        updateBlockCount(finalUserString, 1);
                    }
                    if (meta != null) {
                        Inventory inventory = BlockUtils.getContainerInventory(block.getState(), false);
                        for (Object value : meta) {
                            ItemStack item = ItemUtils.unserializeItemStackLegacy(value);
                            if (item != null) {
                                RollbackUtil.modifyContainerItems(rowType, inventory, 0, item, 1);
                            }
                        }
                    }
                    return false;
                }
                else if (rowType == Material.COMMAND) { // command block
                    BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, false);
                    if (countBlock) {
                        updateBlockCount(finalUserString, 1);
                    }

                    if (meta != null) {
                        CommandBlock commandBlock = (CommandBlock) block.getState();
                        for (Object value : meta) {
                            if (value instanceof String) {
                                String string = (String) value;
                                commandBlock.setCommand(string);
                                commandBlock.update();
                            }
                        }
                    }
                    return false;
                }
                else if ((rowType == Material.WATER) || (rowType == Material.STATIONARY_WATER)) {
                    // In 1.8, no waterlogged blocks - just place water
                    BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, false);
                    return countBlock;
                }
                else if ((rowType == Material.PORTAL) && rowAction == 0) {
                    BlockUtils.prepareTypeAndData(chunkChanges, block, Material.FIRE, (byte) 0, true);
                }
                else if (rowType != Material.AIR && (rowType == Material.BED_BLOCK || rowType.name().endsWith("_BED"))) {
                    // Bed - place foot and head parts
                    // Foot: bits 0-1 = facing, bit 3 = 0
                    // Head: bits 0-1 = facing, bit 3 = 1
                    boolean isHead = (blockData & 0x8) != 0;
                    if (!isHead) {
                        Block headBlock = getBedHead(block, blockData);
                        if (headBlock != null) {
                            byte headData = (byte) (blockData | 0x8);
                            BlockUtils.prepareTypeAndData(chunkChanges, headBlock, rowType, headData, false);
                            if (countBlock) {
                                updateBlockCount(finalUserString, 1);
                            }
                        }
                    }

                    BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, true);
                    return countBlock;
                }
                else if (rowType.name().endsWith("_BANNER")) {
                    BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, false);
                    if (countBlock) {
                        updateBlockCount(finalUserString, 1);
                    }

                    if (meta != null) {
                        Banner banner = (Banner) block.getState();

                        for (Object value : meta) {
                            if (value instanceof DyeColor) {
                                banner.setBaseColor((DyeColor) value);
                            }
                            else if (value instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Pattern pattern = new Pattern((Map<String, Object>) value);
                                banner.addPattern(pattern);
                            }
                        }

                        banner.update();
                    }
                    return false;
                }
                else if (rowType != changeType && (BlockGroup.CONTAINERS.contains(rowType) || BlockGroup.CONTAINERS.contains(changeType))) {
                    block.setType(Material.AIR); // Clear existing container to prevent errors

                    boolean isChest = (rowType == Material.CHEST || rowType == Material.TRAPPED_CHEST);
                    BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, isChest);
                    if (isChest) {
                        ChestTool.updateDoubleChest(block, rowType, blockData, false);
                    }

                    return countBlock;
                }
                else if (BlockGroup.UPDATE_STATE.contains(rowType)) {
                    BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, true);
                    ChestTool.updateDoubleChest(block, rowType, blockData, true);
                    return countBlock;
                }
                else if (rowType != Material.AIR && (BlockGroup.DOORS.contains(rowType) || rowType == Material.IRON_DOOR_BLOCK)) {
                    // Both halves are logged separately in DB - place directly without physics
                    // to avoid door validation dropping the block before both halves exist
                    BlockUtils.setTypeAndData(block, rowType, blockData, false);
                    chunkChanges.remove(block);
                    return countBlock;
                }
                else if (rowType != Material.AIR && rowType == Material.DOUBLE_PLANT) {
                    boolean isTop = (blockData & 0x8) != 0;
                    if (isTop) {
                        Block bottomBlock = block.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ());
                        if (bottomBlock.getType() != Material.DOUBLE_PLANT) {
                            BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, true);
                            return countBlock;
                        }
                    }
                    BlockUtils.setTypeAndData(block, rowType, blockData, false);
                    chunkChanges.remove(block);
                    if (countBlock) {
                        updateBlockCount(finalUserString, 2);
                    }
                    return false;
                }
                else {
                    boolean physics = true;
                    BlockUtils.prepareTypeAndData(chunkChanges, block, rowType, blockData, physics);
                    return countBlock;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if ((rowType != Material.AIR) && changeBlock) {
            if (rowUser.length() > 0) {
                CacheHandler.lookupCache.put(rowX + "." + rowY + "." + rowZ + "." + rowWorldId, new Object[] { unixtimestamp, rowUser, rowType });
            }
        }

        return countBlock;
    }

    /**
     * Get the piston base block from a piston head, based on facing direction encoded in data byte
     */
    private static Block getPistonBase(Block pistonHead, int facing) {
        switch (facing & 0x7) {
            case 0: return pistonHead.getRelative(0, 1, 0);   // down -> base is above
            case 1: return pistonHead.getRelative(0, -1, 0);  // up -> base is below
            case 2: return pistonHead.getRelative(0, 0, 1);   // north -> base is south
            case 3: return pistonHead.getRelative(0, 0, -1);  // south -> base is north
            case 4: return pistonHead.getRelative(1, 0, 0);   // west -> base is east
            case 5: return pistonHead.getRelative(-1, 0, 0);  // east -> base is west
            default: return null;
        }
    }

    /**
     * Get the bed head block from the foot block, based on facing direction in data byte
     */
    private static Block getBedHead(Block footBlock, byte data) {
        int facing = data & 0x3;
        switch (facing) {
            case 0: return footBlock.getRelative(0, 0, 1);   // south
            case 1: return footBlock.getRelative(-1, 0, 0);  // west
            case 2: return footBlock.getRelative(0, 0, -1);  // north
            case 3: return footBlock.getRelative(1, 0, 0);   // east
            default: return null;
        }
    }

    /**
     * Update the block count in the rollback hash
     */
    protected static void updateBlockCount(String userString, int increment) {
        int[] rollbackHashData = ConfigHandler.rollbackHash.get(userString);
        int itemCount = rollbackHashData[0];
        int blockCount = rollbackHashData[1];
        int entityCount = rollbackHashData[2];
        int scannedWorlds = rollbackHashData[4];

        blockCount += increment;
        ConfigHandler.rollbackHash.put(userString, new int[] { itemCount, blockCount, entityCount, 0, scannedWorlds });
    }

    /**
     * Apply all pending block changes to the world
     */
    @SuppressWarnings("deprecation")
    public static void applyBlockChanges(Map<Block, byte[]> chunkChanges, int preview, Player user) {
        for (Entry<Block, byte[]> chunkChange : chunkChanges.entrySet()) {
            Block changeBlock = chunkChange.getKey();
            byte[] changeData = chunkChange.getValue();
            Material changeMaterial = Material.getMaterial(changeData[0] & 0xFF);
            byte changeBlockData = changeData[1];
            if (preview > 0 && user != null) {
                Util.sendBlockChange(user, changeBlock.getLocation(), changeMaterial, changeBlockData);
            }
            else {
                BlockUtils.setTypeAndData(changeBlock, changeMaterial, changeBlockData, true);
            }
        }
        chunkChanges.clear();
    }
}

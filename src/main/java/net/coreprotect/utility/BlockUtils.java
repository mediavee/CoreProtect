package net.coreprotect.utility;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Jukebox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import net.coreprotect.CoreProtect;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.thread.Scheduler;

public class BlockUtils {

    private static final Set<Material> PASSABLE_MATERIALS = new HashSet<>(Arrays.asList(
        Material.AIR, Material.SAPLING, Material.WATER, Material.STATIONARY_WATER,
        Material.LAVA, Material.STATIONARY_LAVA, Material.YELLOW_FLOWER, Material.RED_ROSE,
        Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.FIRE,
        Material.REDSTONE_WIRE, Material.CROPS, Material.SIGN_POST, Material.WALL_SIGN,
        Material.LEVER, Material.STONE_PLATE, Material.WOOD_PLATE,
        Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON,
        Material.STONE_BUTTON, Material.SNOW, Material.SUGAR_CANE_BLOCK,
        Material.PORTAL, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON,
        Material.TRIPWIRE_HOOK, Material.TRIPWIRE, Material.FLOWER_POT,
        Material.CARROT, Material.POTATO, Material.WOOD_BUTTON,
        Material.GOLD_PLATE, Material.IRON_PLATE, Material.REDSTONE_COMPARATOR_OFF,
        Material.REDSTONE_COMPARATOR_ON, Material.ACTIVATOR_RAIL, Material.RAILS,
        Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.CARPET,
        Material.LONG_GRASS, Material.DEAD_BUSH, Material.VINE,
        Material.WATER_LILY, Material.NETHER_WARTS, Material.WEB,
        Material.LADDER, Material.STRING
    ));

    private BlockUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convert a blockdata string to byte array for DB storage.
     * In 1.8, this is only used for paintings/item frames (art name/rotation).
     * Normal blocks use just the data byte in the 'data' column.
     */
    public static byte[] stringToByteData(String string, int type) {
        byte[] result = null;
        if (string != null && !string.isEmpty()) {
            Material material = MaterialUtils.getType(type);
            if (material == null) {
                return result;
            }

            if (material == Material.PAINTING || BukkitAdapter.ADAPTER.isItemFrame(material)) {
                int id = MaterialUtils.getBlockdataId(string, true);
                if (id > -1) {
                    string = Integer.toString(id);
                }
                else {
                    return result;
                }
            }
            else if (material == Material.SKULL) {
                // Store raw data byte as-is for skull rotation
            }
            else {
                return result;
            }

            result = string.getBytes(StandardCharsets.UTF_8);
        }

        return result;
    }

    /**
     * Convert stored byte data back to a string.
     * In 1.8, this is used for paintings/item frames and skull rotation.
     */
    public static String byteDataToString(byte[] data, int type) {
        String result = "";
        if (data != null) {
            Material material = MaterialUtils.getType(type);
            if (material == null) {
                return result;
            }

            result = new String(data, StandardCharsets.UTF_8);
            if (material == Material.SKULL) {
                return result;
            }
            if (result.length() > 0) {
                if (result.matches("\\d+")) {
                    result = result + ",";
                }
                if (result.contains(",")) {
                    String[] blockDataSplit = result.split(",");
                    ArrayList<String> blockDataArray = new ArrayList<>();
                    for (String blockData : blockDataSplit) {
                        String block = MaterialUtils.getBlockDataString(Integer.parseInt(blockData));
                        if (block.length() > 0) {
                            blockDataArray.add(block);
                        }
                    }

                    result = String.join(",", blockDataArray);
                }
                else {
                    result = "";
                }
            }
        }

        return result;
    }

    public static boolean isAir(Material type) {
        return type == Material.AIR;
    }

    public static boolean solidBlock(Material type) {
        return type.isSolid();
    }

    public static boolean passableBlock(Block block) {
        return PASSABLE_MATERIALS.contains(block.getType());
    }

    public static Material getType(Block block) {
        return block.getType();
    }

    @SuppressWarnings("deprecation")
    public static byte getBlockData(Block block) {
        return block.getData();
    }

    @SuppressWarnings("deprecation")
    public static byte getBlockData(BlockState state) {
        return state.getRawData();
    }

    public static String getBlockDataString(BlockState blockState) {
        // In 1.8, return null - block states are captured by data byte
        return null;
    }

    public static boolean iceBreakCheck(BlockState block, String user, Material type) {
        if (type.equals(Material.ICE)) {
            int unixtimestamp = (int) (System.currentTimeMillis() / 1000L);
            int wid = WorldUtils.getWorldId(block.getWorld().getName());
            net.coreprotect.thread.CacheHandler.lookupCache.put("" + block.getX() + "." + block.getY() + "." + block.getZ() + "." + wid + "", new Object[] { unixtimestamp, user, Material.WATER });
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static void prepareTypeAndData(Map<Block, byte[]> map, Block block, Material type, byte data, boolean update) {
        if (!update) {
            setTypeAndData(block, type, data, update);
            map.remove(block);
        }
        else {
            map.put(block, new byte[] { (byte) type.getId(), data });
        }
    }

    @SuppressWarnings("deprecation")
    public static void setTypeAndData(Block block, Material type, byte data, boolean update) {
        if (type != null) {
            block.setTypeIdAndData(type.getId(), data, update);
        }
    }

    public static void updateBlock(final BlockState block) {
        Scheduler.runTask(CoreProtect.getInstance(), () -> {
            try {
                block.update(true, false);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }, block.getLocation());
    }

    public static Inventory getContainerInventory(BlockState blockState, boolean singleBlock) {
        Inventory inventory = null;
        try {
            if (blockState instanceof InventoryHolder) {
                if (singleBlock && blockState instanceof org.bukkit.block.Chest) {
                    inventory = ((org.bukkit.block.Chest) blockState).getBlockInventory();
                }
                if (inventory == null) {
                    inventory = ((InventoryHolder) blockState).getInventory();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return inventory;
    }

    public static List<Object> processMeta(BlockState block) {
        List<Object> meta = new ArrayList<>();
        try {
            if (block instanceof CommandBlock) {
                CommandBlock commandBlock = (CommandBlock) block;
                String command = commandBlock.getCommand();
                if (command.length() > 0) {
                    meta.add(command);
                }
            }
            else if (block instanceof Banner) {
                Banner banner = (Banner) block;
                meta.add(banner.getBaseColor());
                List<Pattern> patterns = banner.getPatterns();
                for (Pattern pattern : patterns) {
                    meta.add(pattern.serialize());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (meta.isEmpty()) {
            meta = null;
        }
        return meta;
    }

    public static ItemStack[] getJukeboxItem(Jukebox blockState) {
        ItemStack[] contents = null;
        try {
            contents = new ItemStack[] { blockState.getPlaying() != null && blockState.getPlaying() != Material.AIR ? new ItemStack(blockState.getPlaying()) : null };
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return contents;
    }

    /**
     * In 1.8, signs don't have glow state. Kept for API compatibility.
     */
    public static int getSignData(boolean frontGlowing, boolean backGlowing) {
        return 0;
    }

    public static boolean isSideGlowing(boolean isFront, int data) {
        return false;
    }

    /**
     * Check if a door data byte represents the top half.
     */
    @SuppressWarnings("deprecation")
    public static boolean isDoorTopHalf(BlockState state) {
        return (state.getRawData() & 0x8) != 0;
    }

    /**
     * Check if a bed data byte represents the head part.
     */
    @SuppressWarnings("deprecation")
    public static boolean isBedHead(BlockState state) {
        return (state.getRawData() & 0x8) != 0;
    }

    /**
     * Check if a double-plant data byte represents the top half.
     */
    @SuppressWarnings("deprecation")
    public static boolean isDoublePlantTop(BlockState state) {
        return (state.getRawData() & 0x8) != 0;
    }
}

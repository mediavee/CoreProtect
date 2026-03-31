package net.coreprotect.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;

public final class BlockGroup {

    public static Set<Material> TRACK_ANY = new HashSet<>(Arrays.asList(Material.PISTON_EXTENSION, Material.LEVER, Material.STONE_BUTTON, Material.WOOD_BUTTON));
    public static Set<Material> TRACK_TOP_BOTTOM = new HashSet<>(Arrays.asList());
    public static Set<Material> TRACK_TOP = new HashSet<>(Arrays.asList(
        Material.TORCH, Material.REDSTONE_TORCH_ON, Material.REDSTONE_TORCH_OFF,
        Material.SAPLING, Material.POWERED_RAIL, Material.DETECTOR_RAIL,
        Material.LONG_GRASS, Material.DEAD_BUSH,
        Material.YELLOW_FLOWER, Material.RED_ROSE,
        Material.BROWN_MUSHROOM, Material.RED_MUSHROOM,
        Material.REDSTONE_WIRE, Material.CROPS,
        Material.SIGN_POST,
        Material.STANDING_BANNER,
        Material.RAILS, Material.IRON_DOOR_BLOCK, Material.SNOW,
        Material.CACTUS, Material.SUGAR_CANE_BLOCK,
        Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON,
        Material.PUMPKIN_STEM, Material.MELON_STEM,
        Material.CARROT, Material.POTATO,
        Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON,
        Material.ACTIVATOR_RAIL,
        Material.DOUBLE_PLANT,
        Material.NETHER_WARTS,
        // Doors
        Material.WOODEN_DOOR, Material.SPRUCE_DOOR, Material.BIRCH_DOOR,
        Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR,
        // Pressure plates
        Material.STONE_PLATE, Material.WOOD_PLATE,
        Material.GOLD_PLATE, Material.IRON_PLATE,
        // Carpet
        Material.CARPET
    ));
    public static Set<Material> TRACK_BOTTOM = new HashSet<>(Arrays.asList(
        Material.WOODEN_DOOR, Material.SPRUCE_DOOR, Material.BIRCH_DOOR,
        Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR,
        Material.IRON_DOOR_BLOCK
    ));
    public static Set<Material> TRACK_SIDE = new HashSet<>(Arrays.asList(
        Material.TORCH, Material.REDSTONE_TORCH_ON, Material.REDSTONE_TORCH_OFF,
        Material.RAILS, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.ACTIVATOR_RAIL,
        Material.BED_BLOCK,
        Material.LADDER,
        Material.WALL_SIGN,
        Material.VINE, Material.COCOA,
        Material.TRIPWIRE_HOOK,
        Material.WALL_BANNER
    ));
    public static Set<Material> SHULKER_BOXES = new HashSet<>(Arrays.asList());
    public static Set<Material> CONTAINERS = new HashSet<>(Arrays.asList(
        Material.JUKEBOX, Material.DISPENSER, Material.CHEST, Material.FURNACE,
        Material.BURNING_FURNACE, Material.BREWING_STAND,
        Material.TRAPPED_CHEST, Material.HOPPER, Material.DROPPER,
        Material.ARMOR_STAND, Material.ITEM_FRAME
    ));
    public static Set<Material> DOORS = new HashSet<>(Arrays.asList(
        Material.WOODEN_DOOR, Material.SPRUCE_DOOR, Material.BIRCH_DOOR,
        Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR
    ));
    public static Set<Material> BUTTONS = new HashSet<>(Arrays.asList(Material.STONE_BUTTON, Material.WOOD_BUTTON));
    public static Set<Material> PRESSURE_PLATES = new HashSet<>(Arrays.asList(
        Material.STONE_PLATE, Material.WOOD_PLATE, Material.GOLD_PLATE, Material.IRON_PLATE
    ));
    public static Set<Material> VINES = new HashSet<>(Arrays.asList(Material.VINE));
    public static Set<Material> AMETHYST = new HashSet<>(Arrays.asList());
    public static Set<Material> FIRE = new HashSet<>(Arrays.asList(Material.FIRE));
    public static Set<Material> SOUL_BLOCKS = new HashSet<>(Arrays.asList(Material.SOUL_SAND));
    public static Set<Material> DIRECTIONAL_BLOCKS = new HashSet<>(Arrays.asList(
        Material.PISTON_STICKY_BASE, Material.PISTON_BASE,
        Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON,
        Material.SKULL, // Includes all skull types in 1.8 via data values
        Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON
    ));
    public static Set<Material> INTERACT_BLOCKS = new HashSet<>(Arrays.asList(
        Material.FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.BIRCH_FENCE_GATE,
        Material.JUNGLE_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.ACACIA_FENCE_GATE,
        Material.DISPENSER, Material.NOTE_BLOCK, Material.CHEST,
        Material.FURNACE, Material.BURNING_FURNACE,
        Material.LEVER, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON,
        Material.TRAP_DOOR,
        Material.BREWING_STAND,
        Material.ANVIL, Material.ENDER_CHEST,
        Material.TRAPPED_CHEST,
        Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON,
        Material.HOPPER, Material.DROPPER,
        Material.WORKBENCH, Material.ENCHANTMENT_TABLE,
        // Doors
        Material.WOODEN_DOOR, Material.SPRUCE_DOOR, Material.BIRCH_DOOR,
        Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR,
        Material.IRON_DOOR_BLOCK,
        // Buttons
        Material.STONE_BUTTON, Material.WOOD_BUTTON
    ));
    public static Set<Material> SAFE_INTERACT_BLOCKS = new HashSet<>(Arrays.asList(
        Material.LEVER, Material.TRAP_DOOR,
        Material.FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.BIRCH_FENCE_GATE,
        Material.JUNGLE_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.ACACIA_FENCE_GATE,
        // Doors
        Material.WOODEN_DOOR, Material.SPRUCE_DOOR, Material.BIRCH_DOOR,
        Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR,
        Material.IRON_DOOR_BLOCK,
        // Buttons
        Material.STONE_BUTTON, Material.WOOD_BUTTON
    ));
    public static Set<Material> UPDATE_STATE = new HashSet<>(Arrays.asList(
        Material.TORCH, Material.REDSTONE_WIRE,
        Material.RAILS, Material.POWERED_RAIL, Material.DETECTOR_RAIL,
        Material.FURNACE, Material.BURNING_FURNACE,
        Material.LEVER, Material.REDSTONE_TORCH_ON, Material.REDSTONE_TORCH_OFF,
        Material.GLOWSTONE, Material.JACK_O_LANTERN,
        Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON,
        Material.REDSTONE_LAMP_ON, Material.REDSTONE_LAMP_OFF,
        Material.BEACON,
        Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON,
        Material.DAYLIGHT_DETECTOR,
        Material.REDSTONE_BLOCK, Material.HOPPER,
        Material.CHEST, Material.TRAPPED_CHEST, Material.ACTIVATOR_RAIL
    ));
    public static Set<Material> NATURAL_BLOCKS = new HashSet<>(Arrays.asList(
        Material.STONE, Material.GOLD_ORE, Material.IRON_ORE, Material.COAL_ORE,
        Material.LAPIS_ORE, Material.SANDSTONE, Material.WEB,
        Material.LONG_GRASS, Material.DEAD_BUSH,
        Material.YELLOW_FLOWER, Material.RED_ROSE,
        Material.BROWN_MUSHROOM, Material.RED_MUSHROOM,
        Material.OBSIDIAN, Material.DIAMOND_ORE, Material.CROPS,
        Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE,
        Material.SNOW, Material.ICE, Material.PACKED_ICE,
        Material.CACTUS, Material.CLAY, Material.SUGAR_CANE_BLOCK,
        Material.PUMPKIN, Material.NETHERRACK, Material.SOUL_SAND,
        Material.MELON_BLOCK, Material.PUMPKIN_STEM, Material.MELON_STEM,
        Material.MYCEL, Material.WATER_LILY,
        Material.NETHER_WARTS, Material.ENDER_STONE,
        Material.EMERALD_ORE, Material.QUARTZ_ORE,
        Material.CARROT, Material.POTATO,
        // Logs
        Material.LOG, Material.LOG_2,
        // Leaves
        Material.LEAVES, Material.LEAVES_2,
        // Dirt types
        Material.GRASS, Material.DIRT, Material.SAND, Material.GRAVEL,
        // Vines
        Material.VINE
    ));
    public static Set<Material> VERTICAL_TOP_BOTTOM = new HashSet<>(Arrays.asList());
    public static Set<Material> VERTICAL_TOP = new HashSet<>(Arrays.asList());
    public static Set<Material> VERTICAL_BOTTOM = new HashSet<>(Arrays.asList());
    public static Set<Material> VERTICAL = new HashSet<>(Arrays.asList());

    public static Set<Material> NON_ATTACHABLE = new HashSet<>(Arrays.asList(
        Material.AIR,
        Material.SAPLING, Material.WATER, Material.STATIONARY_WATER,
        Material.LAVA, Material.STATIONARY_LAVA,
        Material.POWERED_RAIL, Material.DETECTOR_RAIL,
        Material.LONG_GRASS, Material.DEAD_BUSH,
        Material.YELLOW_FLOWER, Material.RED_ROSE,
        Material.BROWN_MUSHROOM, Material.RED_MUSHROOM,
        Material.TORCH, Material.REDSTONE_WIRE,
        Material.LADDER, Material.RAILS,
        Material.LEVER, Material.REDSTONE_TORCH_ON, Material.REDSTONE_TORCH_OFF,
        Material.SNOW, Material.SUGAR_CANE_BLOCK,
        Material.PORTAL, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON,
        Material.STONE_BUTTON, Material.WOOD_BUTTON,
        Material.FIRE
    ));

    public static void initialize() {
        VERTICAL.addAll(VERTICAL_TOP_BOTTOM);
        VERTICAL.addAll(VERTICAL_TOP);
        VERTICAL.addAll(VERTICAL_BOTTOM);

        NATURAL_BLOCKS.addAll(SOUL_BLOCKS);
        NATURAL_BLOCKS.addAll(VINES);
    }
}

package net.coreprotect.listener.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.Database;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.paper.PaperAdapter;
import net.coreprotect.utility.BlockUtils;

public final class BlockBreakListener extends Queue implements Listener {

    @SuppressWarnings("deprecation")
    private static boolean isAttached(Block block, Block scanBlock, int scanMin) {
        Material scanType = scanBlock.getType();
        byte scanData = scanBlock.getData();

        if (!BukkitAdapter.ADAPTER.isAttached(block, scanBlock, scanType, scanData, scanMin)) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    protected static void processBlockBreak(Player player, String user, Block block, boolean logBreak, int skipScan) {
        List<Block> placementMap = new ArrayList<>();
        Material type = block.getType();
        World world = block.getWorld();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        int physics = 0;

        Location[] locationMap = new Location[6];
        locationMap[0] = new Location(world, x + 1, y, z);
        locationMap[1] = new Location(world, x - 1, y, z);
        locationMap[2] = new Location(world, x, y, z + 1);
        locationMap[3] = new Location(world, x, y, z - 1);
        locationMap[4] = new Location(world, x, y + 1, z);
        locationMap[5] = new Location(world, x, y - 1, z);

        int scanMin = 1;
        int scanMax = 8;
        if (!Config.getConfig(world).NATURAL_BREAK) {
            scanMin = 7;
        }
        if (!logBreak) { // log base block breakage
            scanMax = 7;
        }
        while (scanMin < scanMax) {
            Block blockLog = block;
            boolean scanDown = false;
            boolean log = true;

            if (scanMin == skipScan) {
                scanMin++;
                continue;
            }

            if (scanMin < 7) {
                Location scanLocation = locationMap[scanMin - 1];
                Block scanBlock = world.getBlockAt(scanLocation);
                Material scanType = scanBlock.getType();
                if (scanMin == 5) {
                    if (scanType.hasGravity() || false) {
                        if (Config.getConfig(world).BLOCK_MOVEMENT) {
                            // log the top-most sand/gravel block as being removed
                            int scanY = y + 2;
                            boolean topFound = false;
                            while (!topFound) {
                                Block topBlock = world.getBlockAt(x, scanY, z);
                                Material topMaterial = topBlock.getType();
                                if (!topMaterial.hasGravity()) {
                                    scanLocation = new Location(world, x, (scanY - 1), z);
                                    topFound = true;
                                }
                                scanY++;
                            }
                            placementMap.add(scanBlock);
                        }
                    }
                }
                if (!BlockGroup.TRACK_ANY.contains(scanType)) {
                    if (scanMin != 5 && scanMin != 6 && !scanDown) { // side block
                        if (!BlockGroup.TRACK_SIDE.contains(scanType)) {
                            log = false;
                        }
                        else {
                            // determine if side block is attached
                            if (scanType.equals(Material.RAILS) || scanType.equals(Material.POWERED_RAIL) || scanType.equals(Material.DETECTOR_RAIL) || scanType.equals(Material.ACTIVATOR_RAIL)) {
                                byte railData = scanBlock.getData();
                                int shape = railData & 0x7;

                                // Ascending shapes: 2=ASCENDING_EAST, 3=ASCENDING_WEST, 4=ASCENDING_NORTH, 5=ASCENDING_SOUTH
                                if (scanMin == 1 && shape != 3) { // ASCENDING_WEST
                                    log = false;
                                }
                                else if (scanMin == 2 && shape != 2) { // ASCENDING_EAST
                                    log = false;
                                }
                                else if (scanMin == 3 && shape != 4) { // ASCENDING_NORTH
                                    log = false;
                                }
                                else if (scanMin == 4 && shape != 5) { // ASCENDING_SOUTH
                                    log = false;
                                }
                            }
                            else if (scanType.name().endsWith("_BED") && !type.name().endsWith("_BED")) {
                                log = false;
                            }
                            else if (!isAttached(block, scanBlock, scanMin)) {
                                log = false;
                            }
                        }
                    }
                    else { // top/bottom block
                        if (BlockUtil.verticalBreakScan(player, user, block, scanBlock, scanType, scanMin)) {
                            log = false;
                        }
                        else if (scanMin == 5 && (!BlockGroup.TRACK_TOP.contains(scanType) && !BlockGroup.TRACK_TOP_BOTTOM.contains(scanType))) {
                            // top
                            log = false;
                        }
                        else if (scanMin == 6 && (!BlockGroup.TRACK_BOTTOM.contains(scanType) && !BlockGroup.TRACK_TOP_BOTTOM.contains(scanType))) {
                            // bottom
                            log = false;
                        }
                        else if (scanMin == 4 && !BlockGroup.TRACK_TOP.contains(scanType)) {
                            // checking block below for door
                            log = false;
                        }
                        else if (!isAttached(block, scanBlock, scanMin)) {
                            log = false;
                        }
                    }
                    if (!log) {
                        if (type.equals(Material.PISTON_EXTENSION)) {// broke a piston extension
                            if (scanType.equals(Material.PISTON_STICKY_BASE) || scanType.equals(Material.PISTON_BASE)) { // adjacent piston
                                log = true;
                            }
                        }
                        else if (scanMin == 5) {
                            if (scanType.hasGravity() || false) {
                                log = true;
                            }
                        }
                    }
                }
                else {
                    // determine if side block is attached
                    if (scanType.equals(Material.PISTON_EXTENSION)) {
                        if (!type.equals(Material.PISTON_STICKY_BASE) && !type.equals(Material.PISTON_BASE)) {
                            log = false;
                        }
                    }
                    else if (BlockGroup.BUTTONS.contains(scanType) || scanType == Material.LEVER) {
                        boolean scanButton = BukkitAdapter.ADAPTER.isAttached(block, scanBlock, scanBlock.getType(), scanBlock.getData(), scanMin);
                        if (!scanButton) {
                            log = false;
                        }
                    }
                    else if (!isAttached(block, scanBlock, scanMin)) {
                        log = false;
                    }
                }
                if (log) {
                    blockLog = world.getBlockAt(scanLocation);
                }
            }

            int blockNumber = scanMin;
            Material blockType = blockLog.getType();
            BlockState blockState = blockLog.getState();

            if (log && (blockType.name().toUpperCase(Locale.ROOT).endsWith("_BANNER") || blockType.equals(Material.SKULL))) {
                try {
                    if (blockState instanceof Banner || blockState instanceof Skull) {
                        Queue.queueAdvancedBreak(user, blockState, blockType, null, 0, type, blockNumber);
                    }
                    log = false;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (log && BukkitAdapter.ADAPTER.isSign(blockType)) {
                if (Config.getConfig(world).SIGN_TEXT) {
                    try {
                        Location location = blockState.getLocation();
                        Sign sign = (Sign) blockLog.getState();
                        String line1 = PaperAdapter.ADAPTER.getLine(sign, 0);
                        String line2 = PaperAdapter.ADAPTER.getLine(sign, 1);
                        String line3 = PaperAdapter.ADAPTER.getLine(sign, 2);
                        String line4 = PaperAdapter.ADAPTER.getLine(sign, 3);
                        String line5 = PaperAdapter.ADAPTER.getLine(sign, 4);
                        String line6 = PaperAdapter.ADAPTER.getLine(sign, 5);
                        String line7 = PaperAdapter.ADAPTER.getLine(sign, 6);
                        String line8 = PaperAdapter.ADAPTER.getLine(sign, 7);

                        boolean isFront = true;
                        int color = BukkitAdapter.ADAPTER.getColor(sign, isFront);
                        int colorSecondary = BukkitAdapter.ADAPTER.getColor(sign, !isFront);
                        boolean frontGlowing = BukkitAdapter.ADAPTER.isGlowing(sign, isFront);
                        boolean backGlowing = BukkitAdapter.ADAPTER.isGlowing(sign, !isFront);
                        boolean isWaxed = BukkitAdapter.ADAPTER.isWaxed(sign);

                        Queue.queueSignText(user, location, 0, color, colorSecondary, frontGlowing, backGlowing, isWaxed, isFront, line1, line2, line3, line4, line5, line6, line7, line8, 5);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (log) {
                Database.containerBreakCheck(user, blockType, blockLog, null, blockLog.getLocation());
                Queue.queueBlockBreak(user, blockState, blockType, null, type, physics, blockNumber);

                if (player != null && BlockUtils.iceBreakCheck(blockState, user, blockType)) {
                    ItemStack handItem = player.getItemInHand();
                    if (!(player.getGameMode().equals(GameMode.CREATIVE)) && !(handItem != null && handItem.containsEnchantment(Enchantment.SILK_TOUCH))) {
                        Queue.queueBlockPlaceValidate(user, blockState, blockLog, null, Material.WATER, -1, 0, null, 0);
                    }
                }
            }

            scanMin++;
        }

        for (Block placementBlock : placementMap) {
            Material placementType = placementBlock.getType();
            if (placementType.hasGravity()) {
                queueBlockPlace(user, block.getState(), placementType, null, null, -1, 0, null);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            String user = event.getPlayer().getName();
            Block block = event.getBlock();
            Config config = Config.getConfig(block.getWorld());
            if (config.BLOCK_BREAK_BLACKLIST.contains(block.getType())) {
                return;
            }
            processBlockBreak(event.getPlayer(), user, event.getBlock(), config.BLOCK_BREAK, BlockUtil.NONE);
        }
    }

}

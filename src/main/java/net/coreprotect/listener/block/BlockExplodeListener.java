package net.coreprotect.listener.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.Database;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.paper.PaperAdapter;

public final class BlockExplodeListener extends Queue implements Listener {

    @SuppressWarnings("deprecation")
    public static void processBlockExplode(String user, World world, List<Block> blockList) {
        HashMap<Location, Block> blockMap = new HashMap<>();

        for (Block block : blockList) {
            blockMap.put(block.getLocation(), block);
        }

        if (Config.getConfig(world).NATURAL_BREAK) {
            for (Entry<Location, Block> data : new HashMap<>(blockMap).entrySet()) {
                Block block = data.getValue();
                int x = block.getX();
                int y = block.getY();
                int z = block.getZ();

                Location[] locationMap = new Location[5];
                locationMap[0] = new Location(world, (x + 1), y, z);
                locationMap[1] = new Location(world, (x - 1), y, z);
                locationMap[2] = new Location(world, x, y, (z + 1));
                locationMap[3] = new Location(world, x, y, (z - 1));
                locationMap[4] = new Location(world, x, (y + 1), z);

                int scanMin = 0;
                int scanMax = 5;
                while (scanMin < scanMax) {
                    Location location = locationMap[scanMin];
                    if (blockMap.get(location) == null) {
                        Block scanBlock = world.getBlockAt(location);
                        Material scanType = scanBlock.getType();
                        if (BlockGroup.TRACK_ANY.contains(scanType) || BlockGroup.TRACK_TOP.contains(scanType) || BlockGroup.TRACK_TOP_BOTTOM.contains(scanType) || BlockGroup.TRACK_BOTTOM.contains(scanType) || BlockGroup.TRACK_SIDE.contains(scanType)) {
                            blockMap.put(location, scanBlock);

                            // Properly log double blocks (doors, double plants)
                            if (BlockGroup.DOORS.contains(scanType) || scanType == Material.IRON_DOOR_BLOCK || scanType == Material.DOUBLE_PLANT) {
                                boolean isTopHalf = (scanBlock.getData() & 0x8) != 0;
                                Location bisectLocation = location.clone();
                                if (isTopHalf) {
                                    bisectLocation.setY(bisectLocation.getY() - 1);
                                }
                                else {
                                    bisectLocation.setY(bisectLocation.getY() + 1);
                                }

                                int worldMaxHeight = world.getMaxHeight();
                                if (bisectLocation.getBlockY() >= 0 && bisectLocation.getBlockY() < worldMaxHeight && blockMap.get(bisectLocation) == null) {
                                    blockMap.put(bisectLocation, world.getBlockAt(bisectLocation));
                                }
                            }
                        }
                        else if (scanType.hasGravity() && Config.getConfig(world).BLOCK_MOVEMENT) {
                            int scanY = location.getBlockY() + 1;
                            boolean topFound = false;
                            while (!topFound) {
                                Block topBlock = world.getBlockAt(location.getBlockX(), scanY, location.getBlockZ());
                                Material topMaterial = topBlock.getType();
                                if (!topMaterial.hasGravity()) {
                                    location = new Location(world, location.getBlockX(), (scanY - 1), location.getBlockZ());
                                    topFound = true;

                                    if (BlockGroup.TRACK_ANY.contains(topMaterial) || BlockGroup.TRACK_TOP.contains(topMaterial) || BlockGroup.TRACK_TOP_BOTTOM.contains(topMaterial)) {
                                        blockMap.put(topBlock.getLocation(), topBlock);
                                    }
                                }
                                scanY++;
                            }

                            Block gravityBlock = location.getBlock();
                            blockMap.put(location, gravityBlock);
                            Queue.queueBlockGravityValidate(user, location, gravityBlock, scanType, 0);
                        }
                    }
                    scanMin++;
                }
            }
        }

        for (Map.Entry<Location, Block> entry : blockMap.entrySet()) {
            Block block = entry.getValue();
            Material blockType = block.getType();
            BlockState blockState = block.getState();
            if (BukkitAdapter.ADAPTER.isSign(blockType) && Config.getConfig(world).SIGN_TEXT) {
                try {
                    Location location = blockState.getLocation();
                    Sign sign = (Sign) blockState;
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

            Database.containerBreakCheck(user, blockType, block, null, block.getLocation());
            Queue.queueBlockBreak(user, blockState, blockType, null, 0);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onBlockExplode(BlockExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        World world = event.getBlock().getLocation().getWorld();
        if (!Config.getConfig(world).EXPLOSIONS) {
            return;
        }

        // In 1.8, block explosions are typically from TNT or beds
        Material blockType = event.getBlock().getType();
        String user = "#explosion";
        if (blockType.name().contains("_BED") || blockType == Material.BED_BLOCK) {
            user = "#bed";
        }

        processBlockExplode(user, world, event.blockList());
    }

}

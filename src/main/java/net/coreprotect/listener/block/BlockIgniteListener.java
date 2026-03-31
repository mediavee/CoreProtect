package net.coreprotect.listener.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.inventory.ItemStack;

import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.logger.ItemLogger;
import net.coreprotect.listener.player.ProjectileLaunchListener;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.thread.CacheHandler;
import net.coreprotect.utility.WorldUtils;

public final class BlockIgniteListener extends Queue implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onBlockIgnite(BlockIgniteEvent event) {
        World world = event.getBlock().getWorld();
        if (!event.isCancelled() && Config.getConfig(world).BLOCK_IGNITE) {
            Block block = event.getBlock();
            if (block == null) {
                return;
            }

            Material blockType = block.getType();
            Material blockIgnited = Material.FIRE;
            Location scanLocation = block.getLocation().clone();
            if (scanLocation.getY() > 0) {
                scanLocation.setY(scanLocation.getY() - 1);
                Material blockBelow = scanLocation.getBlock().getType();

                if (BlockGroup.SOUL_BLOCKS.contains(blockBelow)) {
                    for (Material fire : BlockGroup.FIRE) {
                        if (fire != blockIgnited) {
                            blockIgnited = fire;
                            break;
                        }
                    }
                }
                else if (event.getCause() == IgniteCause.ENDER_CRYSTAL && blockBelow == Material.AIR) {
                    return;
                }
            }

            if (event.getPlayer() == null) {
                if (blockIgnited == Material.FIRE && event.getCause() == IgniteCause.LAVA && event.getIgnitingBlock() != null) {
                    boolean burnableBlock = false;
                    for (BlockFace face : BlockFace.values()) {
                        Location blockLocation = block.getLocation();
                        scanLocation.setX(blockLocation.getX());
                        scanLocation.setY(blockLocation.getY());
                        scanLocation.setZ(blockLocation.getZ());

                        switch (face) {
                            case NORTH:
                                scanLocation.setZ(scanLocation.getZ() - 1);
                                break;
                            case SOUTH:
                                scanLocation.setZ(scanLocation.getZ() + 1);
                                break;
                            case WEST:
                                scanLocation.setX(scanLocation.getX() - 1);
                                break;
                            case EAST:
                                scanLocation.setX(scanLocation.getX() + 1);
                                break;
                            case DOWN:
                                scanLocation.setY(scanLocation.getY() - 1);
                                break;
                            case UP:
                                scanLocation.setY(scanLocation.getY() + 1);
                                break;
                            default:
                                continue;
                        }

                        if (scanLocation.getY() < 0 || scanLocation.getY() >= world.getMaxHeight()) {
                            continue;
                        }

                        if (scanLocation.getBlock().getType().isBurnable()) {
                            burnableBlock = true;
                            break;
                        }
                    }

                    if (!burnableBlock) {
                        return;
                    }
                }

                Queue.queueBlockPlace("#fire", block.getState(), block.getType(), null, blockIgnited, -1, 0, null);
            }
            else {
                if (event.getCause() == IgniteCause.FIREBALL) {
                    ProjectileLaunchListener.playerLaunchProjectile(event.getPlayer().getLocation(), event.getPlayer().getName(), new ItemStack(Material.FIREBALL), 1, -1, 1, ItemLogger.ITEM_THROW);
                }

                Player player = event.getPlayer();
                Queue.queueBlockPlace(player.getName(), block.getState(), block.getType(), null, blockIgnited, -1, 0, null);
                int unixtimestamp = (int) (System.currentTimeMillis() / 1000L);
                int world_id = WorldUtils.getWorldId(block.getWorld().getName());
                CacheHandler.lookupCache.put("" + block.getX() + "." + block.getY() + "." + block.getZ() + "." + world_id + "", new Object[] { unixtimestamp, player.getName(), block.getType() });
            }
        }
    }
}

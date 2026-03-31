package net.coreprotect.listener.block;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;
import net.coreprotect.listener.player.InventoryChangeListener;
import net.coreprotect.thread.CacheHandler;

public final class BlockDispenseListener extends Queue implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    protected void onBlockDispense(BlockDispenseEvent event) {
        Block block = event.getBlock();
        World world = block.getWorld();
        if (!event.isCancelled() && Config.getConfig(world).BLOCK_PLACE) {
            Material blockType = block.getType();
            ItemStack item = event.getItem();
            if (item != null && (blockType == Material.DISPENSER || blockType == Material.DROPPER)) {
                MaterialData materialData = block.getState().getData();
                BlockFace facing = BlockFace.SELF;
                if (materialData instanceof org.bukkit.material.Dispenser) {
                    facing = ((org.bukkit.material.Dispenser) materialData).getFacing();
                }
                else if (materialData instanceof org.bukkit.material.DirectionalContainer) {
                    facing = ((org.bukkit.material.DirectionalContainer) materialData).getFacing();
                }

                Material material = item.getType();
                Material type = Material.AIR;
                String user = "#dispenser";
                boolean forceItem = true;

                Block newBlock = block.getRelative(facing);
                Location velocityLocation = event.getVelocity().toLocation(world);
                boolean dispenseSuccess = !event.getVelocity().equals(new Vector()); // true if velocity is set
                boolean dispenseRelative = newBlock.getLocation().equals(velocityLocation); // true if velocity location matches relative location

                if (dispenseRelative || material.equals(Material.FLINT_AND_STEEL) || material.equals(Material.SHEARS)) {
                    forceItem = false;
                }

                if (blockType == Material.DROPPER) {
                    forceItem = true; // droppers always drop items
                }

                ItemStack[] inventory = ((InventoryHolder) block.getState()).getInventory().getContents();
                if (forceItem) {
                    inventory = Arrays.copyOf(inventory, inventory.length + 1);
                    inventory[inventory.length - 1] = item;
                }
                InventoryChangeListener.inventoryTransaction(user, block.getLocation(), inventory);

                if (material.equals(Material.WATER_BUCKET)) {
                    type = Material.WATER;
                    user = "#water";
                }
                else if (material.equals(Material.LAVA_BUCKET)) {
                    type = Material.LAVA;
                    user = "#lava";
                }
                else if (material.equals(Material.FLINT_AND_STEEL)) {
                    type = Material.FIRE;
                    user = "#fire";
                }

                if (!dispenseSuccess && material == Material.INK_SACK) {
                    CacheHandler.redstoneCache.put(newBlock.getLocation(), new Object[] { System.currentTimeMillis(), user });
                }

                if (type == Material.FIRE && !Config.getConfig(world).BLOCK_IGNITE) {
                    return;
                }
                else if (type != Material.FIRE && (!Config.getConfig(world).BUCKETS || (!Config.getConfig(world).WATER_FLOW && type.equals(Material.WATER)) || (!Config.getConfig(world).LAVA_FLOW && type.equals(Material.LAVA)))) {
                    return;
                }

                if (!type.equals(Material.AIR) || !newBlock.getType().equals(Material.AIR)) {
                    if (dispenseRelative) {
                        BlockState blockState = newBlock.getState();

                        if (!type.equals(Material.AIR)) {
                            queueBlockPlace(user, newBlock.getState(), newBlock.getType(), blockState, type, 1, 1, null);
                        }
                        else {
                            Queue.queueBlockBreak(user, newBlock.getState(), newBlock.getType(), null, 0);
                        }
                    }
                }
            }
        }
    }

}

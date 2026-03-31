package net.coreprotect.listener.player;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import net.coreprotect.language.Phrase;
import net.coreprotect.listener.player.inspector.BlockInspector;
import net.coreprotect.listener.player.inspector.ContainerInspector;
import net.coreprotect.listener.player.inspector.InteractionInspector;
import net.coreprotect.listener.player.inspector.SignInspector;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.thread.CacheHandler;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.ItemUtils;
import net.coreprotect.utility.Util;
import net.coreprotect.utility.WorldUtils;

@SuppressWarnings("deprecation")
public final class PlayerInteractListener extends Queue implements Listener {

    public static ConcurrentHashMap<String, Object[]> lastInspectorEvent = new ConcurrentHashMap<>();

    private final BlockInspector blockInspector = new BlockInspector();
    private final SignInspector signInspector = new SignInspector();
    private final ContainerInspector containerInspector = new ContainerInspector();
    private final InteractionInspector interactionInspector = new InteractionInspector();

    @EventHandler(priority = EventPriority.LOWEST)
    protected void onPlayerInspect(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (!Boolean.TRUE.equals(ConfigHandler.inspecting.get(player.getName()))) {
            return;
        }

        if (!player.hasPermission("coreprotect.inspect")) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
            ConfigHandler.inspecting.put(player.getName(), false);
            return;
        }

        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            BlockState checkBlock = event.getClickedBlock().getState();

            /* Check if clicking top half of door (use block below) */
            Block clickedBlock = event.getClickedBlock();
            Material clickedType = clickedBlock.getType();
            if (BlockGroup.DOORS.contains(clickedType)) {
                Block blockBelow = world.getBlockAt(clickedBlock.getX(), clickedBlock.getY() - 1, clickedBlock.getZ());
                if (blockBelow.getType() == clickedType) {
                    checkBlock = blockBelow.getState();
                }
            }

            blockInspector.performBlockLookup(player, checkBlock);

            Block block = event.getClickedBlock();
            int blockX = block.getX();
            int blockY = block.getY();
            int blockZ = block.getZ();

            Block x1 = world.getBlockAt(blockX + 1, blockY, blockZ);
            Block x2 = world.getBlockAt(blockX - 1, blockY, blockZ);
            Block z1 = world.getBlockAt(blockX, blockY, blockZ + 1);
            Block z2 = world.getBlockAt(blockX, blockY, blockZ - 1);
            Util.sendBlockChange(player, x1.getLocation(), x1.getType(), x1.getData());
            Util.sendBlockChange(player, x2.getLocation(), x2.getType(), x2.getData());
            Util.sendBlockChange(player, z1.getLocation(), z1.getType(), z1.getData());
            Util.sendBlockChange(player, z2.getLocation(), z2.getType(), z2.getData());

            event.setCancelled(true);
        }
        else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = event.getClickedBlock();
            if (block != null) {
                final Material type = block.getType();
                boolean isInteractBlock = BlockGroup.INTERACT_BLOCKS.contains(type);
                boolean isContainerBlock = BlockGroup.CONTAINERS.contains(type);
                boolean isSignBlock = BukkitAdapter.ADAPTER.isSign(type);

                if (isInteractBlock || isContainerBlock || isSignBlock) {
                    final Block clickedBlock = event.getClickedBlock();

                    if (isSignBlock) {
                        Location location = clickedBlock.getLocation();
                        signInspector.performSignLookup(player, location);
                        event.setCancelled(true);
                    }
                    else if (isContainerBlock && Config.getConfig(world).ITEM_TRANSACTIONS) {
                        Location location = null;
                        if (type.equals(Material.CHEST) || type.equals(Material.TRAPPED_CHEST)) {
                            Chest chest = (Chest) clickedBlock.getState();
                            InventoryHolder inventoryHolder = chest.getInventory().getHolder();

                            if (inventoryHolder instanceof DoubleChest) {
                                DoubleChest doubleChest = (DoubleChest) inventoryHolder;
                                location = doubleChest.getLocation();
                            }
                            else {
                                location = chest.getLocation();
                            }
                        }

                        if (location == null) {
                            location = clickedBlock.getLocation();
                        }

                        containerInspector.performContainerLookup(player, location);
                        event.setCancelled(true);
                    }
                    else if (isInteractBlock) {
                        Block interactBlock = clickedBlock;
                        if (BlockGroup.DOORS.contains(type)) {
                            int y = interactBlock.getY() - 1;
                            Block blockUnder = interactBlock.getWorld().getBlockAt(interactBlock.getX(), y, interactBlock.getZ());

                            if (blockUnder.getType().equals(type)) {
                                interactBlock = blockUnder;
                            }
                        }

                        interactionInspector.performInteractionLookup(player, interactBlock);

                        if (!BlockGroup.SAFE_INTERACT_BLOCKS.contains(type) || player.isSneaking()) {
                            event.setCancelled(true);
                        }
                    }
                }
                else {
                    boolean performLookup = true;
                    String uuid = event.getPlayer().getUniqueId().toString();
                    long systemTime = System.currentTimeMillis();

                    if (lastInspectorEvent.get(uuid) != null) {
                        Object[] lastEvent = lastInspectorEvent.get(uuid);
                        long lastTime = (long) lastEvent[0];

                        long timeSince = systemTime - lastTime;
                        if (timeSince < 50) {
                            performLookup = false;
                        }
                    }

                    if (performLookup) {
                        final BlockState finalBlock = event.getClickedBlock().getRelative(event.getBlockFace()).getState();
                        blockInspector.performAirBlockLookup(player, finalBlock);

                        ItemUtils.updateInventory(event.getPlayer());
                        lastInspectorEvent.put(uuid, new Object[] { systemTime });
                    }

                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            World world = event.getClickedBlock().getWorld();
            if (event.useInteractedBlock() != Event.Result.DENY) {
                Block block = event.getClickedBlock();
                if (block.getType() == Material.DRAGON_EGG) {
                    PlayerInteractUtils.clickedDragonEgg(event.getPlayer(), block);
                }

                if (Config.getConfig(world).BLOCK_BREAK) {
                    Block relativeBlock = event.getClickedBlock().getRelative(event.getBlockFace());

                    if (BlockGroup.FIRE.contains(relativeBlock.getType())) {
                        Player player = event.getPlayer();
                        Material type = relativeBlock.getType();
                        Queue.queueBlockBreak(player.getName(), relativeBlock.getState(), type, null, 0);
                    }
                }
            }
        }
        else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            World world = player.getWorld();

            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && block != null) {
                final Material type = block.getType();
                if (event.useInteractedBlock() != Event.Result.DENY) {
                    if (BukkitAdapter.ADAPTER.isSign(type)) {
                        // sign interaction logging not applicable in 1.8
                    }
                    else if (BlockGroup.INTERACT_BLOCKS.contains(type)) {
                        if (player.getItemInHand() != null && Config.getConfig(world).PLAYER_INTERACTIONS) {
                            Block interactBlock = event.getClickedBlock();
                            if (BlockGroup.DOORS.contains(type)) {
                                int y = interactBlock.getY() - 1;
                                Block blockUnder = interactBlock.getWorld().getBlockAt(interactBlock.getX(), y, interactBlock.getZ());

                                if (blockUnder.getType().equals(type)) {
                                    interactBlock = blockUnder;
                                }
                            }

                            Queue.queuePlayerInteraction(player.getName(), interactBlock.getState(), type);
                        }
                    }
                    else if (type == Material.JUKEBOX) {
                        BlockState blockState = block.getState();
                        if (blockState instanceof Jukebox) {
                            Jukebox jukebox = (Jukebox) blockState;
                            ItemStack jukeboxRecord = jukebox.isPlaying() ? new ItemStack(jukebox.getPlaying()) : new ItemStack(Material.AIR);
                            ItemStack oldItemState = jukeboxRecord.clone();
                            ItemStack newItemState = new ItemStack(Material.AIR);

                            if (jukeboxRecord.getType() == Material.AIR) {
                                ItemStack handItem = player.getItemInHand();
                                if (handItem != null && handItem.getType().name().startsWith("RECORD_")) {
                                    oldItemState = new ItemStack(Material.AIR);
                                    newItemState = handItem.clone();
                                }
                                else {
                                    return;
                                }
                            }

                            if (!oldItemState.equals(newItemState)) {
                                if (Config.getConfig(player.getWorld()).PLAYER_INTERACTIONS) {
                                    Queue.queuePlayerInteraction(player.getName(), blockState, type);
                                }

                                if (Config.getConfig(block.getWorld()).ITEM_TRANSACTIONS) {
                                    boolean logDrops = player.getGameMode() != GameMode.CREATIVE;
                                    ItemStack[] oldState = new ItemStack[] { oldItemState };
                                    ItemStack[] newState = new ItemStack[] { newItemState };
                                    PlayerInteractEntityListener.queueContainerSpecifiedItems(player.getName(), Material.JUKEBOX, new Object[] { oldState, newState }, jukebox.getLocation(), logDrops);
                                }
                            }
                        }
                    }
                    else if (type == Material.DRAGON_EGG) {
                        PlayerInteractUtils.clickedDragonEgg(player, block);
                    }

                    if (type == Material.CAKE_BLOCK) {
                        String userUUID = player.getUniqueId().toString();
                        Location location = player.getLocation();
                        long time = System.currentTimeMillis();
                        int wid = WorldUtils.getWorldId(location.getWorld().getName());
                        int x = location.getBlockX();
                        int y = location.getBlockY();
                        int z = location.getBlockZ();
                        String coordinates = x + "." + y + "." + z + "." + wid + "." + userUUID;
                        CacheHandler.interactCache.put(coordinates, new Object[] { time, Material.CAKE_BLOCK, block.getState() });
                    }
                }
            }

            if (event.useItemInHand() != Event.Result.DENY) {
                List<Material> entityBlockTypes = Arrays.asList(Material.ARMOR_STAND, Material.BOW, Material.EXP_BOTTLE, Material.ENDER_PEARL, Material.EGG, Material.SNOW_BALL);
                ItemStack handItem = player.getItemInHand();

                if (handItem == null || !entityBlockTypes.contains(handItem.getType())) {
                    return;
                }

                if (handItem.getType().equals(Material.ARMOR_STAND)) {
                    if (block == null) {
                        return;
                    }

                    Block relativeBlock = block.getRelative(event.getBlockFace());
                    Location relativeBlockLocation = relativeBlock.getLocation();
                    Location blockLocation = block.getLocation();

                    String relativeBlockKey = world.getName() + "-" + relativeBlockLocation.getBlockX() + "-" + relativeBlockLocation.getBlockY() + "-" + relativeBlockLocation.getBlockZ();
                    String blockKey = world.getName() + "-" + blockLocation.getBlockX() + "-" + blockLocation.getBlockY() + "-" + blockLocation.getBlockZ();
                    Object[] keys = new Object[] { System.currentTimeMillis(), relativeBlockKey, blockKey, handItem };
                    ConfigHandler.entityBlockMapper.put(player.getName(), keys);
                }
                else {
                    Location relativeBlockLocation = player.getLocation().clone();
                    relativeBlockLocation.setY(relativeBlockLocation.getY() + 1);
                    Location blockLocation = relativeBlockLocation.clone();
                    blockLocation.setY(blockLocation.getY() + 1);

                    String relativeBlockKey = world.getName() + "-" + relativeBlockLocation.getBlockX() + "-" + relativeBlockLocation.getBlockY() + "-" + relativeBlockLocation.getBlockZ();
                    String blockKey = world.getName() + "-" + blockLocation.getBlockX() + "-" + blockLocation.getBlockY() + "-" + blockLocation.getBlockZ();
                    Object[] keys = new Object[] { System.currentTimeMillis(), relativeBlockKey, blockKey, handItem };
                    ConfigHandler.entityBlockMapper.put(player.getName(), keys);
                }
            }
        }
        else if (event.getAction().equals(Action.PHYSICAL)) {
            Block block = event.getClickedBlock();
            if (block == null || !block.getType().equals(Material.SOIL)) {
                return;
            }

            World world = block.getWorld();
            if (event.useInteractedBlock() != Event.Result.DENY && Config.getConfig(world).BLOCK_BREAK) {
                Player player = event.getPlayer();
                if (block.getType().equals(Material.SOIL)) {
                    Block blockAbove = world.getBlockAt(block.getX(), block.getY() + 1, block.getZ());
                    Material type = blockAbove.getType();

                    if (!type.equals(Material.AIR)) {
                        Queue.queueBlockBreak(player.getName(), blockAbove.getState(), type, null, 0);
                    }
                }

                Queue.queueBlockBreak(player.getName(), block.getState(), block.getType(), null, 0);
                Queue.queueBlockPlaceDelayed(player.getName(), block.getLocation(), block.getType(), null, null, 0);
            }
        }
    }
}

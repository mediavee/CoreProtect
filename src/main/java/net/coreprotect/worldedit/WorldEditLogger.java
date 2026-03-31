package net.coreprotect.worldedit;

import java.util.Locale;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.platform.Actor;

import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;
import net.coreprotect.utility.EntityUtils;

public class WorldEditLogger extends Queue {

    public static WorldEditPlugin getWorldEdit(Server server) {
        Plugin plugin = server.getPluginManager().getPlugin("WorldEdit");
        if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
            return null;
        }
        return (WorldEditPlugin) plugin;
    }

    public static boolean isInitialized() {
        return CoreProtectEditSessionEvent.isInitialized();
    }

    @SuppressWarnings("deprecation")
    protected static void postProcess(Actor actor, Vector position, Location location, BaseBlock newBlock, BaseBlock oldBlock, Material oldType, ItemStack[] containerContents) {
        Material newType = Material.getMaterial(newBlock.getId());
        if (newType == null) {
            newType = Material.AIR;
        }
        if (oldType == null) {
            oldType = Material.AIR;
        }

        int oldBlockId = oldBlock.getId();
        byte oldBlockData = (byte) oldBlock.getData();
        int newBlockId = newBlock.getId();
        byte newBlockData = (byte) newBlock.getData();

        org.bukkit.block.BlockState oldBlockState = new WorldEditBlockState(location, oldType, oldBlockData);
        org.bukkit.block.BlockState newBlockState = new WorldEditBlockState(location, newType, newBlockData);

        int oldBlockExtraData = 0;

        if (oldBlockId != newBlockId || oldBlockData != newBlockData) {
            try {
                if (oldBlock.hasNbtData() && Config.getConfig(location.getWorld()).SIGN_TEXT && net.coreprotect.bukkit.BukkitAdapter.ADAPTER.isSign(oldType)) {
                    CompoundTag compoundTag = oldBlock.getNbtData();
                    String line1 = getSignText(compoundTag.getString("Text1"));
                    String line2 = getSignText(compoundTag.getString("Text2"));
                    String line3 = getSignText(compoundTag.getString("Text3"));
                    String line4 = getSignText(compoundTag.getString("Text4"));
                    int color = 0;
                    try {
                        color = DyeColor.valueOf(compoundTag.getString("Color").toUpperCase()).getColor().asRGB();
                    }
                    catch (Exception e) {
                        // Color not available in 1.8 sign NBT
                    }
                    Queue.queueSignText(actor.getName(), location, 0, color, 0, false, false, false, true, line1, line2, line3, line4, "", "", "", "", 5);
                }

                if (oldBlock.hasNbtData() && oldType == Material.MOB_SPAWNER) {
                    String mobType = getMobType(oldBlock);
                    if (mobType != null) {
                        try {
                            EntityType entityType = EntityType.valueOf(mobType);
                            oldBlockExtraData = EntityUtils.getSpawnerType(entityType);
                        }
                        catch (IllegalArgumentException exception) {
                            // mobType isn't a valid enum
                        }
                    }
                }

                if (containerContents != null) {
                    Queue.queueContainerBreak(actor.getName(), location, oldType, containerContents);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if (oldType == Material.AIR && newType != Material.AIR) {
                Queue.queueBlockPlace(actor.getName(), newBlockState, newType, null, newType, -1, 0, null);
            }
            else if (oldType != Material.AIR && newType != Material.AIR) {
                Queue.queueBlockBreak(actor.getName(), oldBlockState, oldBlockState.getType(), null, null, oldBlockExtraData, 0);
                Queue.queueBlockPlace(actor.getName(), newBlockState, newType, null, newType, -1, 0, null);
            }
            else if (oldType != Material.AIR && newType == Material.AIR) {
                Queue.queueBlockBreak(actor.getName(), oldBlockState, oldBlockState.getType(), null, null, oldBlockExtraData, 0);
            }
        }
    }

    private static String getMobType(BaseBlock fullBlock) {
        String mobType = null;
        try {
            CompoundTag compoundTag = NBTUtils.getChildTag(fullBlock.getNbtData().getValue(), "SpawnData", CompoundTag.class);
            mobType = compoundTag.getString("id").toUpperCase(Locale.ROOT);
            if (mobType.contains("MINECRAFT:")) {
                String[] mobTypeSplit = mobType.split(":");
                mobType = mobTypeSplit[1];
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return mobType;
    }

    private static String getSignText(String line) {
        if (line == null || line.isEmpty()) {
            return "";
        }

        if (!line.startsWith("{\"text\":\"")) {
            return line;
        }

        try {
            org.json.simple.JSONObject json = (org.json.simple.JSONObject) new org.json.simple.parser.JSONParser().parse(line);
            return (String) json.get("text");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}

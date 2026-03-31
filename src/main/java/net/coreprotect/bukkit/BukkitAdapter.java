package net.coreprotect.bukkit;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Attachable;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;

import net.coreprotect.utility.BlockUtils;

public class BukkitAdapter implements BukkitInterface {

    public static BukkitInterface ADAPTER;

    public static void loadAdapter() {
        ADAPTER = new BukkitAdapter();
    }

    @Override
    public String parseLegacyName(String name) {
        return name;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLegacyBlockId(Material material) {
        return material.getId();
    }

    @Override
    public boolean getEntityMeta(LivingEntity entity, List<Object> info) {
        return false;
    }

    @Override
    public boolean setEntityMeta(Entity entity, Object value, int count) {
        return false;
    }

    @Override
    public EntityType getEntityType(Material material) {
        if (material.name().equals("ENDER_CRYSTAL") || material.name().equals("END_CRYSTAL")) {
            return EntityType.ENDER_CRYSTAL;
        }
        return EntityType.UNKNOWN;
    }

    @Override
    public boolean getItemMeta(ItemMeta itemMeta, List<Map<String, Object>> list, List<List<Map<String, Object>>> metadata, int slot) {
        return false;
    }

    @Override
    public boolean setItemMeta(Material rowType, ItemStack itemstack, List<Map<String, Object>> map) {
        return false;
    }

    @Override
    public Material getPlantSeeds(Material material) {
        switch (material) {
            case CROPS:
                return Material.SEEDS;
            case PUMPKIN_STEM:
                return Material.PUMPKIN_SEEDS;
            case MELON_STEM:
                return Material.MELON_SEEDS;
            default:
                return material;
        }
    }

    @Override
    public ItemStack getArrowMeta(Arrow arrow, ItemStack itemStack) {
        // In 1.8, tipped arrows don't exist
        return itemStack;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isAttached(Block block, Block scanBlock, Material scanType, byte scanData, int scanMin) {
        MaterialData data = scanType.getNewData(scanData);

        if (data instanceof Button || data instanceof Lever) {
            if (data instanceof Attachable) {
                BlockFace attachedFace = ((Attachable) data).getAttachedFace();
                Block attachedBlock = scanBlock.getRelative(attachedFace);
                return attachedBlock.getLocation().equals(block.getLocation());
            }
        }

        // For other attachable blocks, default to true (unvalidated)
        return true;
    }

    @Override
    public Material getBucketContents(Material material) {
        if (material == Material.WATER_BUCKET) {
            return Material.WATER;
        }
        else if (material == Material.LAVA_BUCKET) {
            return Material.LAVA;
        }
        return Material.AIR;
    }

    @Override
    public boolean isInvisible(Material material) {
        return BlockUtils.isAir(material);
    }

    @Override
    public boolean isItemFrame(Material material) {
        return material == Material.ITEM_FRAME;
    }

    @Override
    public Material getFrameType(Entity entity) {
        return Material.ITEM_FRAME;
    }

    @Override
    public Material getFrameType(EntityType type) {
        return type == EntityType.ITEM_FRAME ? Material.ITEM_FRAME : null;
    }

    @Override
    public Class<?> getFrameClass(Material material) {
        return ItemFrame.class;
    }

    @Override
    public boolean isSign(Material material) {
        return material == Material.SIGN_POST || material == Material.WALL_SIGN;
    }

    @Override
    public String getLine(Sign sign, int line) {
        if (line >= 0 && line < 4) {
            return sign.getLine(line);
        }
        return "";
    }

    @Override
    public void setLine(Sign sign, int line, String string) {
        if (string == null) {
            string = "";
        }
        if (line >= 0 && line < 4) {
            sign.setLine(line, string);
        }
    }

    @Override
    public int getColor(Sign sign, boolean isFront) {
        return 0; // Signs don't have dye color in 1.8
    }

    @Override
    public void setColor(Sign sign, boolean isFront, int color) {
        // No-op in 1.8
    }

    @Override
    public boolean isGlowing(Sign sign, boolean isFront) {
        return false;
    }

    @Override
    public void setGlowing(Sign sign, boolean isFront, boolean isGlowing) {
        // No-op in 1.8
    }

    @Override
    public boolean isWaxed(Sign sign) {
        return false;
    }

    @Override
    public void setWaxed(Sign sign, boolean isWaxed) {
        // No-op in 1.8
    }
}

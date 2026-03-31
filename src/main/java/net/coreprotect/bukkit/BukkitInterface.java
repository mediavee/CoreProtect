package net.coreprotect.bukkit;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public interface BukkitInterface {

    boolean isAttached(Block block, Block scanBlock, Material scanType, byte scanData, int scanMin);

    int getLegacyBlockId(Material material);

    Material getBucketContents(Material material);

    boolean isItemFrame(Material material);

    boolean isInvisible(Material material);

    boolean isSign(Material material);

    Material getPlantSeeds(Material material);

    boolean getItemMeta(ItemMeta itemMeta, List<Map<String, Object>> list, List<List<Map<String, Object>>> metadata, int slot);

    boolean setItemMeta(Material rowType, ItemStack itemstack, List<Map<String, Object>> map);

    ItemStack getArrowMeta(Arrow arrow, ItemStack itemStack);

    boolean getEntityMeta(LivingEntity entity, List<Object> info);

    boolean setEntityMeta(Entity entity, Object value, int count);

    Material getFrameType(Entity entity);

    Material getFrameType(EntityType type);

    EntityType getEntityType(Material material);

    Class<?> getFrameClass(Material material);

    String getLine(Sign sign, int line);

    void setLine(Sign sign, int line, String string);

    int getColor(Sign sign, boolean isFront);

    void setColor(Sign sign, boolean isFront, int color);

    boolean isGlowing(Sign sign, boolean isFront);

    void setGlowing(Sign sign, boolean isFront, boolean isGlowing);

    boolean isWaxed(Sign sign);

    void setWaxed(Sign sign, boolean isWaxed);

    String parseLegacyName(String name);
}

package net.coreprotect.database.rollback;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.io.BukkitObjectInputStream;

import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.Lookup;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.utility.ItemUtils;

public class RollbackUtil extends Lookup {

    protected static int modifyContainerItems(Material type, Object container, int slot, ItemStack itemstack, int action) {
        int modifiedArmor = -1;
        try {
            ItemStack[] contents = null;

            if (type != null && type.equals(Material.ARMOR_STAND)) {
                EntityEquipment equipment = (EntityEquipment) container;
                if (equipment != null) {
                    if (action == 1) {
                        itemstack.setAmount(1);
                    }
                    else {
                        itemstack.setType(Material.AIR);
                        itemstack.setAmount(0);
                    }

                    if (slot < 4) {
                        contents = equipment.getArmorContents();
                        if (slot >= 0) {
                            contents[slot] = itemstack;
                        }
                        equipment.setArmorContents(contents);
                    }
                    else if (slot == 4) {
                        ArmorStand armorStand = (ArmorStand) equipment.getHolder();
                        armorStand.setArms(true);
                        equipment.setItemInHand(itemstack);
                    }
                }
            }
            else if (type != null && type.equals(Material.ITEM_FRAME)) {
                ItemFrame frame = (ItemFrame) container;
                if (frame != null) {
                    if (action == 1) {
                        itemstack.setAmount(1);
                    }
                    else {
                        itemstack.setType(Material.AIR);
                        itemstack.setAmount(0);
                    }

                    frame.setItem(itemstack);
                }
            }
            else if (type != null && type.equals(Material.JUKEBOX)) {
                Jukebox jukebox = (Jukebox) container;
                if (jukebox != null) {
                    if (action == 1 && itemstack.getType().name().startsWith("RECORD_")) {
                        itemstack.setAmount(1);
                        jukebox.setPlaying(itemstack.getType());
                    }
                    else {
                        jukebox.setPlaying(Material.AIR);
                    }
                    jukebox.update();
                }
            }
            else {
                Inventory inventory = (Inventory) container;
                if (inventory != null) {
                    boolean isPlayerInventory = (inventory instanceof PlayerInventory);
                    if (action == 1) {
                        int count = 0;
                        int amount = itemstack.getAmount();
                        itemstack.setAmount(1);

                        while (count < amount) {
                            boolean addedItem = false;
                            if (isPlayerInventory) {
                                int setArmor = ItemUtils.setPlayerArmor((PlayerInventory) inventory, itemstack);
                                addedItem = (setArmor > -1);
                                modifiedArmor = addedItem ? setArmor : modifiedArmor;
                            }
                            if (!addedItem) {
                                addedItem = (inventory.addItem(itemstack).size() == 0);
                            }
                            // No offhand slot in 1.8
                            count++;
                        }
                    }
                    else {
                        int removeAmount = itemstack.getAmount();
                        ItemStack removeMatch = itemstack.clone();
                        removeMatch.setAmount(1);

                        ItemStack[] inventoryContents = (isPlayerInventory ? inventory.getContents() : inventory.getContents()).clone();
                        for (int i = inventoryContents.length - 1; i >= 0; i--) {
                            if (inventoryContents[i] != null) {
                                ItemStack itemStack = inventoryContents[i].clone();
                                int maxAmount = itemStack.getAmount();
                                int currentAmount = maxAmount;
                                itemStack.setAmount(1);

                                if (itemStack.toString().equals(removeMatch.toString())) {
                                    for (int scan = 0; scan < maxAmount; scan++) {
                                        if (removeAmount > 0) {
                                            currentAmount--;
                                            itemStack.setAmount(currentAmount);
                                            removeAmount--;
                                        }
                                        else {
                                            break;
                                        }
                                    }
                                }
                                else {
                                    itemStack.setAmount(maxAmount);
                                }

                                if (itemStack.getAmount() == 0) {
                                    inventoryContents[i] = null;
                                }
                                else {
                                    inventoryContents[i] = itemStack;
                                }
                            }

                            if (removeAmount == 0) {
                                break;
                            }
                        }

                        if (isPlayerInventory) {
                            inventory.setContents(inventoryContents);
                        }
                        else {
                            inventory.setContents(inventoryContents);
                        }

                        int count = 0;
                        while (count < removeAmount) {
                            inventory.removeItem(removeMatch);
                            count++;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return modifiedArmor;
    }

    public static void sortContainerItems(PlayerInventory inventory, List<Integer> modifiedArmorSlots) {
        try {
            ItemStack[] armorContents = inventory.getArmorContents();
            ItemStack[] storageContents = inventory.getContents();

            for (int armor = 0; armor < armorContents.length; armor++) {
                ItemStack armorItem = armorContents[armor];
                if (armorItem == null || !modifiedArmorSlots.contains(armor)) {
                    continue;
                }

                for (int storage = 0; storage < storageContents.length; storage++) {
                    ItemStack storageItem = storageContents[storage];
                    if (storageItem == null) {
                        storageContents[storage] = armorItem;
                        armorContents[armor] = null;
                        break;
                    }
                }
            }

            inventory.setArmorContents(armorContents);
            inventory.setContents(storageContents);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void buildFireworkEffect(Builder effectBuilder, Material rowType, ItemStack itemstack) {
        try {
            FireworkEffect effect = effectBuilder.build();
            if ((rowType == Material.FIREWORK)) {
                FireworkMeta meta = (FireworkMeta) itemstack.getItemMeta();
                meta.addEffect(effect);
                itemstack.setItemMeta(meta);
            }
            else if ((rowType == Material.FIREWORK_CHARGE)) {
                FireworkEffectMeta meta = (FireworkEffectMeta) itemstack.getItemMeta();
                meta.setEffect(effect);
                itemstack.setItemMeta(meta);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static Object[] populateItemStack(ItemStack itemstack, Object list) {
        int slot = 0;
        String faceData = "";

        try {
            /*
            if (list instanceof Object[]) {
                slot = (int) ((Object[]) list)[0];
                ItemMeta itemMeta = (ItemMeta) ((Object[]) list)[1];
                itemstack.setItemMeta(itemMeta);
                return new Object[] { slot, itemstack };
            }
            */

            Material rowType = itemstack.getType();
            List<Object> metaList = (List<Object>) list;
            if (metaList.size() > 0 && !(metaList.get(0) instanceof List<?>)) {
                if (rowType.name().endsWith("_BANNER")) {
                    BannerMeta meta = (BannerMeta) itemstack.getItemMeta();
                    for (Object value : metaList) {
                        if (value instanceof Map) {
                            Pattern pattern = new Pattern((Map<String, Object>) value);
                            meta.addPattern(pattern);
                        }
                    }
                    itemstack.setItemMeta(meta);
                }
                return new Object[] { slot, faceData, itemstack };
            }

            int itemCount = 0;
            Builder effectBuilder = FireworkEffect.builder();
            for (List<Map<String, Object>> map : (List<List<Map<String, Object>>>) list) {
                if (map.size() == 0) {
                    if (itemCount == 3 && (rowType == Material.FIREWORK || rowType == Material.FIREWORK_CHARGE)) {
                        buildFireworkEffect(effectBuilder, rowType, itemstack);
                        itemCount = 0;
                    }

                    itemCount++;
                    continue;
                }
                Map<String, Object> mapData = map.get(0);

                if (mapData.get("slot") != null) {
                    slot = (Integer) mapData.get("slot");
                }
                else if (mapData.get("facing") != null) {
                    faceData = (String) mapData.get("facing");
                }
                else if (itemCount == 0) {
                    ItemMeta meta = ItemUtils.deserializeItemMeta(itemstack.getItemMeta().getClass(), map.get(0));
                    itemstack.setItemMeta(meta);

                    // PotionMeta.setColor() not available in 1.8
                }
                else {
                    if ((rowType == Material.LEATHER_HELMET) || (rowType == Material.LEATHER_CHESTPLATE) || (rowType == Material.LEATHER_LEGGINGS) || (rowType == Material.LEATHER_BOOTS)) { // leather armor
                        for (Map<String, Object> colorData : map) {
                            LeatherArmorMeta meta = (LeatherArmorMeta) itemstack.getItemMeta();
                            org.bukkit.Color color = org.bukkit.Color.deserialize(colorData);
                            meta.setColor(color);
                            itemstack.setItemMeta(meta);
                        }
                    }
                    else if ((rowType == Material.POTION)) { // potion
                        for (Map<String, Object> potionData : map) {
                            PotionMeta meta = (PotionMeta) itemstack.getItemMeta();
                            PotionEffect effect = new PotionEffect(potionData);
                            meta.addCustomEffect(effect, true);
                            itemstack.setItemMeta(meta);
                        }
                    }
                    else if (rowType.name().endsWith("_BANNER")) {
                        for (Map<String, Object> patternData : map) {
                            BannerMeta meta = (BannerMeta) itemstack.getItemMeta();
                            Pattern pattern = new Pattern(patternData);
                            meta.addPattern(pattern);
                            itemstack.setItemMeta(meta);
                        }
                    }
                    // MapMeta.setColor() not available in 1.8
                    else if ((rowType == Material.FIREWORK) || (rowType == Material.FIREWORK_CHARGE)) {
                        if (itemCount == 1) {
                            effectBuilder = FireworkEffect.builder();
                            for (Map<String, Object> fireworkData : map) {
                                org.bukkit.FireworkEffect.Type type = (org.bukkit.FireworkEffect.Type) fireworkData.getOrDefault("type", org.bukkit.FireworkEffect.Type.BALL);
                                boolean hasFlicker = (Boolean) fireworkData.get("flicker");
                                boolean hasTrail = (Boolean) fireworkData.get("trail");
                                effectBuilder.with(type);
                                effectBuilder.flicker(hasFlicker);
                                effectBuilder.trail(hasTrail);
                            }
                        }
                        else if (itemCount == 2) {
                            for (Map<String, Object> colorData : map) {
                                org.bukkit.Color color = org.bukkit.Color.deserialize(colorData);
                                effectBuilder.withColor(color);
                            }
                        }
                        else if (itemCount == 3) {
                            for (Map<String, Object> colorData : map) {
                                org.bukkit.Color color = org.bukkit.Color.deserialize(colorData);
                                effectBuilder.withFade(color);
                            }
                            buildFireworkEffect(effectBuilder, rowType, itemstack);
                            itemCount = 0;
                        }
                    }
                    else {
                        BukkitAdapter.ADAPTER.setItemMeta(rowType, itemstack, map);
                    }
                }

                itemCount++;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new Object[] { slot, faceData, itemstack };
    }

    public static Object[] populateItemStack(ItemStack itemstack, byte[] metadata) {
        if (metadata != null) {
            try {
                ByteArrayInputStream metaByteStream = new ByteArrayInputStream(metadata);
                BukkitObjectInputStream metaObjectStream = new BukkitObjectInputStream(metaByteStream);
                Object metaList = metaObjectStream.readObject();
                metaObjectStream.close();
                metaByteStream.close();

                return populateItemStack(itemstack, metaList);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new Object[] { 0, "", itemstack };
    }

    /**
     * Deserializes metadata from a byte array into a list of objects.
     *
     * @param metadata
     *            The byte array containing serialized metadata
     * @return The deserialized list of objects or null if deserialization fails
     */
    public static List<Object> deserializeMetadata(byte[] metadata) {
        if (metadata == null) {
            return null;
        }

        try {
            ByteArrayInputStream metaByteStream = new ByteArrayInputStream(metadata);
            BukkitObjectInputStream metaObjectStream = new BukkitObjectInputStream(metaByteStream);
            @SuppressWarnings("unchecked")
            List<Object> metaList = (List<Object>) metaObjectStream.readObject();
            metaObjectStream.close();
            metaByteStream.close();
            return metaList;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Queues an entity spawn operation for processing.
     *
     * @param user
     *            The username of the player
     * @param block
     *            The block state where the entity should be spawned
     * @param type
     *            The type of entity to spawn
     * @param data
     *            Additional data for the entity
     */
    public static void queueEntitySpawn(String user, BlockState block, EntityType type, int data) {
        Queue.queueEntitySpawn(user, block, type, data);
    }

    /**
     * Queues a skull update operation for processing.
     *
     * @param user
     *            The username of the player
     * @param block
     *            The block state to update
     * @param rowId
     *            The row ID for the skull data
     */
    public static void queueSkullUpdate(String user, BlockState block, int rowId) {
        Queue.queueSkullUpdate(user, block, rowId);
    }

    /**
     * Queues a sign update operation for processing.
     *
     * @param user
     *            The username of the player
     * @param block
     *            The block state to update
     * @param action
     *            The action type
     * @param time
     *            The time of the update
     */
    public static void queueSignUpdate(String user, BlockState block, int action, int time) {
        Queue.queueSignUpdate(user, block, action, time);
    }
}

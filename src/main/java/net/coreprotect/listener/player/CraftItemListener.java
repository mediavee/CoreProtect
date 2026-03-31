package net.coreprotect.listener.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.logger.ItemLogger;

public final class CraftItemListener extends Queue implements Listener {

    protected static void logCraftedItem(Location location, String user, ItemStack itemStack, int action) {
        if (!Config.getConfig(location.getWorld()).ITEM_TRANSACTIONS || itemStack == null) {
            return;
        }

        String loggingItemId = user.toLowerCase(Locale.ROOT) + "." + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ();
        int itemId = getItemId(loggingItemId);

        if (action == ItemLogger.ITEM_BUY) {
            List<ItemStack> list = ConfigHandler.itemsBuy.getOrDefault(loggingItemId, new ArrayList<>());
            list.add(itemStack);
            ConfigHandler.itemsBuy.put(loggingItemId, list);
        }
        else if (action == ItemLogger.ITEM_SELL) {
            List<ItemStack> list = ConfigHandler.itemsSell.getOrDefault(loggingItemId, new ArrayList<>());
            list.add(itemStack);
            ConfigHandler.itemsSell.put(loggingItemId, list);
        }
        else if (action == ItemLogger.ITEM_CREATE) {
            List<ItemStack> list = ConfigHandler.itemsCreate.getOrDefault(loggingItemId, new ArrayList<>());
            list.add(itemStack);
            ConfigHandler.itemsCreate.put(loggingItemId, list);
        }
        else {
            List<ItemStack> list = ConfigHandler.itemsDestroy.getOrDefault(loggingItemId, new ArrayList<>());
            list.add(itemStack);
            ConfigHandler.itemsDestroy.put(loggingItemId, list);
        }

        int time = (int) (System.currentTimeMillis() / 1000L) + 1;
        Queue.queueItemTransaction(user, location.clone(), time, 0, itemId);
    }

    protected static void playerCraftItem(InventoryClickEvent event, boolean isTrade) {
        if (event.getResult() == Result.DENY || event.getSlotType() != SlotType.RESULT) {
            return;
        }

        HumanEntity player = event.getWhoClicked();
        if (event.getClick() == ClickType.NUMBER_KEY && player.getInventory().getItem(event.getHotbarButton()) != null) {
            return;
        }

        if ((event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP) && event.getCursor().getType() != Material.AIR) {
            return;
        }

        Inventory bottomInventory = player.getInventory();
        if (bottomInventory.getType() != InventoryType.PLAYER) {
            return;
        }

        Recipe recipe = null;
        if (!isTrade && event instanceof CraftItemEvent) {
            recipe = ((CraftItemEvent) event).getRecipe();
        }
        if (recipe == null) {
            return;
        }

        ItemStack addItem = recipe.getResult().clone();
        int amount = addItem.getAmount();
        if (amount == 0) {
            return;
        }

        int amountMultiplier = 1;
        if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
            int newMultiplier = Integer.MIN_VALUE;
            for (ItemStack item : event.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR && !item.equals(recipe.getResult())) {
                    if (newMultiplier == Integer.MIN_VALUE || item.getAmount() < newMultiplier) {
                        newMultiplier = item.getAmount();
                    }
                }
            }
            amountMultiplier = (newMultiplier == Integer.MIN_VALUE ? 1 : newMultiplier);

            int addAmount = amount * amountMultiplier;
            Inventory virtualInventory = Bukkit.createInventory(null, 36);
            virtualInventory.setContents(bottomInventory.getContents());
            addItem.setAmount(1);

            int addSuccess = 0;
            for (int i = 0; i < addAmount; i++) {
                if (!virtualInventory.addItem(addItem).isEmpty()) {
                    break;
                }
                addSuccess++;
            }

            if (addSuccess < addAmount) {
                addAmount = (int) (Math.ceil(addSuccess / (double) amount) * amount);
                amountMultiplier = addAmount / amount;
            }

            virtualInventory.clear();
            addItem.setAmount(addAmount);
        }

        List<ItemStack> oldItems = new ArrayList<>();
        if (recipe instanceof ShapelessRecipe) {
            oldItems.addAll(((ShapelessRecipe) recipe).getIngredientList());
        }
        else if (recipe instanceof ShapedRecipe) {
            oldItems.addAll(((ShapedRecipe) recipe).getIngredientMap().values());
        }

        if (addItem.getAmount() > 0) {
            Location location = player.getLocation();
            for (ItemStack oldItem : oldItems) {
                if (oldItem == null || oldItem.getType() == Material.AIR) {
                    continue;
                }

                ItemStack removedItem = oldItem.clone();
                removedItem.setAmount(removedItem.getAmount() * amountMultiplier);
                logCraftedItem(location, player.getName(), removedItem, ItemLogger.ITEM_DESTROY);
            }

            logCraftedItem(location, player.getName(), addItem, ItemLogger.ITEM_CREATE);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    protected void onCraftItem(CraftItemEvent event) {
        playerCraftItem(event, false);
    }

}

package com.winthier.custom_tnt;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.inventory.CustomInventory;
import com.winthier.custom.item.CustomItem;
import com.winthier.custom.item.ItemDescription;
import com.winthier.custom.util.Dirty;
import com.winthier.custom.util.Msg;
import java.util.EnumMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class BombBagInventory implements CustomInventory {
    private final CustomTNTPlugin plugin;
    private final Player player;
    private final ItemStack bombBagItem;
    @Getter private final Inventory inventory;

    BombBagInventory(CustomTNTPlugin plugin, Player player, ItemStack bombBagItem) {
        this.plugin = plugin;
        this.player = player;
        this.bombBagItem = bombBagItem;
        this.inventory = Bukkit.getServer().createInventory(player, 18, "Bomb Bag");
        ItemStack storeIcon = new ItemStack(Material.CHEST);
        ItemDescription desc = new ItemDescription();
        desc.setCategory(plugin.getConfig().getString("bomb_bag_inventory.category.Store"));
        desc.setUsage(plugin.getConfig().getString("bomb_bag_inventory.usage.Store"));
        desc.apply(storeIcon);
        inventory.setItem(0, storeIcon);
    }

    private void populateItems() {
        Dirty.TagWrapper config = Dirty.TagWrapper.getItemConfigOf(bombBagItem);
        for (CustomTNTType type: CustomTNTType.values()) {
            int amount = config.getInt(type.key);
            if (amount > 0) {
                ItemStack icon = plugin.getItems().get(type).spawnItemStack(Math.max(1, Math.min(64, amount)));
                ItemDescription desc = plugin.getItems().get(type).getItemDescription().clone();
                desc.setCategory(plugin.getConfig().getString("bomb_bag_inventory.category.Fetch"));
                desc.setUsage(plugin.getConfig().getString("bomb_bag_inventory.usage.Fetch"));
                desc.getStats().put("Amount", "" + config.getInt(type.key));
                desc.apply(icon);
                inventory.setItem(type.ordinal() + 1, icon);
            } else {
                inventory.setItem(type.ordinal() + 1, null);
            }
        }
    }

    @Override
    public void onInventoryOpen(InventoryOpenEvent event) {
        player.playSound(player.getEyeLocation(), Sound.ENTITY_HORSE_ARMOR, 1.0f, 0.65f);
        populateItems();
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        player.playSound(player.getEyeLocation(), Sound.ENTITY_HORSE_ARMOR, 1.0f, 2.0f);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!event.isLeftClick()) return;
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(inventory)) return;
        int slot = event.getSlot();
        if (slot == 0) {
            int total = storeAll();
            if (total > 0) {
                player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 0.5f);
                Msg.sendActionBar(player, "&aStored %d explosives.", total);
                populateItems();
                plugin.getBombBagItem().updateBagDescription(bombBagItem);
            } else {
                Msg.sendActionBar(player, "&cNothing found!");
                player.playSound(player.getEyeLocation(), Sound.BLOCK_DISPENSER_FAIL, 1.0f, 0.55f);
            }
        } else if (slot <= CustomTNTType.values().length) {
            int amount = event.isShiftClick() ? 64 : 1;
            int total = fetch(CustomTNTType.values()[slot - 1], amount);
            if (total > 0) {
                Msg.sendActionBar(player, "&aRetrieved %d explosives.", total);
                populateItems();
                plugin.getBombBagItem().updateBagDescription(bombBagItem);
            } else {
                Msg.sendActionBar(player, "&cNothing found!");
                player.playSound(player.getEyeLocation(), Sound.BLOCK_DISPENSER_FAIL, 1.0f, 0.55f);
            }
        }
    }

    private int storeAll() {
        Map<CustomTNTType, Integer> amounts = new EnumMap<>(CustomTNTType.class);
        int total = 0;
        for (CustomTNTType type: CustomTNTType.values()) amounts.put(type, 0);
        Inventory playerInv = player.getInventory();
        for (int i = 0; i < playerInv.getSize(); ++i) {
            ItemStack item = playerInv.getItem(i);
            CustomItem customItem = CustomPlugin.getInstance().getItemManager().getCustomItem(item);
            if (customItem != null && customItem instanceof CustomTNTItem) {
                CustomTNTItem customTNTItem = (CustomTNTItem)customItem;
                CustomTNTType type = customTNTItem.getType();
                total += item.getAmount();
                amounts.put(type, amounts.get(type) + item.getAmount());
                playerInv.setItem(i, null);
            }
        }
        if (total == 0) return 0;
        Dirty.TagWrapper config = Dirty.TagWrapper.getItemConfigOf(bombBagItem);
        for (CustomTNTType type: CustomTNTType.values()) {
            int amount = amounts.get(type);
            if (amount > 0) {
                config.setInt(type.key, config.getInt(type.key) + amount);
            }
        }
        return total;
    }

    private int fetch(CustomTNTType type, int amount) {
        Dirty.TagWrapper config = Dirty.TagWrapper.getItemConfigOf(bombBagItem);
        int has = config.getInt(type.key);
        amount = Math.min(amount, has);
        if (amount <= 0) return 0;
        config.setInt(type.key, has - amount);
        CustomPlugin.getInstance().getItemManager().dropItemStack(player.getEyeLocation(), type.customId, amount).setPickupDelay(0);
        return amount;
    }
}

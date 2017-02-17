package com.winthier.custom_tnt;

import com.winthier.custom.CustomConfig;
import com.winthier.custom.CustomPlugin;
import com.winthier.custom.item.CustomItem;
import com.winthier.custom.util.Dirty;
import com.winthier.custom.util.Msg;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class CustomTNTItem implements CustomItem {
    private final CustomTNTPlugin plugin;
    private final String customId;
    private final ItemStack itemStack;

    CustomTNTItem(CustomTNTPlugin plugin, String customId) {
        this.plugin = plugin;
        this.customId = customId;
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        item = Dirty.setSkullOwner(item, "Custom TNT",
                                   new UUID(0, 0),
                                   "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGJkZmZmZGNhMDdkNTYyYmY5OTA1MGU5MGM0ZGI1ZmE1OGM1MDk4MmU1NWNmOTZhOGQxMDIyNTZhZmZhZjUifX19"); // Violet TNT
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 0, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(Msg.format("&rCustom TNT"));
        item.setItemMeta(meta);
        this.itemStack = item;
    }

    @Override
    public String getCustomId() {
        return customId;
    }

    @Override
    public ItemStack spawnItemStack(int amount, CustomConfig config) {
        ItemStack result = itemStack.clone();
        result.setAmount(amount);
        return result;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        CustomPlugin.getInstance().getBlockManager().setBlock(event.getBlock(), customId);
    }
}

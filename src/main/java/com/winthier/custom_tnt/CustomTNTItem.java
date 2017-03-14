package com.winthier.custom_tnt;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.item.CustomItem;
import com.winthier.custom.item.ItemDescription;
import com.winthier.custom.util.Dirty;
import com.winthier.custom.util.Msg;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class CustomTNTItem implements CustomItem {
    private final CustomTNTPlugin plugin;
    private final CustomTNTType type;
    private final String customId;
    private final ItemStack itemStack;

    CustomTNTItem(CustomTNTPlugin plugin, CustomTNTType type) {
        this.plugin = plugin;
        this.type = type;
        this.customId = type.customId;
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("tnt_types").getConfigurationSection(type.key);
        item = Dirty.setSkullOwner(item,
                                   config.getString("DisplayName"),
                                   UUID.fromString(config.getString("Id", UUID.randomUUID().toString())),
                                   config.getString("Texture"));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Msg.format("&r%s", config.getString("DisplayName")));
        item.setItemMeta(meta);
        ItemDescription description = new ItemDescription();
        description.setCategory("Explosive");
        description.setDescription(config.getString("Description", null));
        description.setUsage(plugin.getConfig().getString("lore.Usage"));
        description.apply(item);
        this.itemStack = item;
    }

    @Override
    public String getCustomId() {
        return customId;
    }

    @Override
    public ItemStack spawnItemStack(int amount) {
        ItemStack result = itemStack.clone();
        result.setAmount(amount);
        return result;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        CustomPlugin.getInstance().getBlockManager().wrapBlock(event.getBlock(), customId);
    }
}

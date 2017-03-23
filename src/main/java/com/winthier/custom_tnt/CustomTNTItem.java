package com.winthier.custom_tnt;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.item.CustomItem;
import com.winthier.custom.item.ItemDescription;
import com.winthier.custom.util.Dirty;
import com.winthier.custom.util.Msg;
import com.winthier.generic_events.GenericEventsPlugin;
import com.winthier.generic_events.ItemNameEvent;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Getter
public final class CustomTNTItem implements CustomItem {
    private final CustomTNTPlugin plugin;
    private final CustomTNTType type;
    private final String customId;
    private final ItemStack itemStack;
    private final String displayName;
    private final ItemDescription itemDescription;
    private final String noGriefWarning;

    CustomTNTItem(CustomTNTPlugin plugin, CustomTNTType type) {
        this.plugin = plugin;
        this.type = type;
        this.customId = type.customId;
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("types").getConfigurationSection(type.key);
        item = Dirty.setSkullOwner(item,
                                   config.getString("DisplayName"),
                                   UUID.fromString(config.getString("Id")),
                                   config.getString("Texture"));
        ItemMeta meta = item.getItemMeta();
        this.displayName = config.getString("DisplayName");
        meta.setDisplayName(Msg.format("&r%s", displayName));
        item.setItemMeta(meta);
        ItemDescription description = new ItemDescription();
        description.setCategory("Explosive");
        description.setDescription(config.getString("Description"));
        description.setUsage(plugin.getConfig().getString("lore.Usage"));
        description.apply(item);
        this.itemStack = item;
        this.itemDescription = description;
        this.noGriefWarning = plugin.getConfig().getString("NoGriefWarning");
    }

    @Override
    public ItemStack spawnItemStack(int amount) {
        ItemStack result = itemStack.clone();
        result.setAmount(amount);
        return result;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!GenericEventsPlugin.getInstance().playerCanGrief(event.getPlayer(), event.getBlock())) {
            Msg.sendActionBar(event.getPlayer(), noGriefWarning);
            event.setCancelled(true);
            return;
        }
        CustomPlugin.getInstance().getBlockManager().wrapBlock(event.getBlock(), customId);
    }

    @EventHandler
    public void onItemName(ItemNameEvent event) {
        event.setItemName(displayName);
    }
}

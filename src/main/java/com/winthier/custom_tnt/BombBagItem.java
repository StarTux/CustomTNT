package com.winthier.custom_tnt;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.item.CustomItem;
import com.winthier.custom.item.ItemContext;
import com.winthier.custom.item.ItemDescription;
import com.winthier.custom.util.Dirty;
import com.winthier.custom.util.Msg;
import com.winthier.generic_events.ItemNameEvent;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Getter
public final class BombBagItem implements CustomItem {
    private final CustomTNTPlugin plugin;
    private final ItemStack itemStack;
    private final String displayName;
    private final String customId = "tnt:bomb_bag";
    private final ItemDescription itemDescription;

    BombBagItem(CustomTNTPlugin plugin) {
        this.plugin = plugin;
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("bomb_bag_item");
        item = Dirty.setSkullOwner(item,
                                   config.getString("DisplayName"),
                                   UUID.fromString(config.getString("Id")),
                                   config.getString("Texture"));
        ItemMeta meta = item.getItemMeta();
        this.displayName = config.getString("DisplayName");
        meta.setDisplayName(Msg.format("&r%s", displayName));
        item.setItemMeta(meta);
        ItemDescription description = new ItemDescription();
        description.setCategory("Storage");
        description.setDescription(config.getString("Description"));
        description.setUsage(config.getString("Usage"));
        description.apply(item);
        this.itemStack = item;
        this.itemDescription = description;
    }

    @Override
    public ItemStack spawnItemStack(int amount) {
        ItemStack result = itemStack.clone();
        result.setAmount(amount);
        return result;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event, ItemContext context) {
        event.setCancelled(true);
        switch (event.getAction()) {
        case RIGHT_CLICK_BLOCK:
        case RIGHT_CLICK_AIR:
            openBag(event.getPlayer(), context.getItemStack());
            break;
        default:
            break;
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event, ItemContext context) {
        event.setCancelled(true);
        openBag(event.getPlayer(), context.getItemStack());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onItemName(ItemNameEvent event) {
        event.setItemName(displayName);
    }

    void openBag(Player player, ItemStack item) {
        CustomPlugin.getInstance().getInventoryManager().openInventory(player, new BombBagInventory(plugin, player, item));
    }
}
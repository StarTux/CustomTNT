package com.winthier.custom_tnt;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.item.CustomItem;
import com.winthier.custom.item.ItemContext;
import com.winthier.custom.item.ItemDescription;
import com.winthier.custom.item.UncraftableItem;
import com.winthier.custom.util.Dirty;
import com.winthier.custom.util.Msg;
import com.winthier.generic_events.ItemNameEvent;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Getter
public final class BombBagItem implements CustomItem, UncraftableItem {
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
        switch (event.getAction()) {
        case RIGHT_CLICK_BLOCK:
            if (event.getPlayer().isSneaking()) return;
        case RIGHT_CLICK_AIR:
            event.setCancelled(true);
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event, ItemContext context) {
        plugin.getBombBagBlock().save(CustomPlugin.getInstance().getBlockManager().wrapBlock(event.getBlock(), customId), context.getItemStack());
    }

    @EventHandler
    public void onItemName(ItemNameEvent event) {
        event.setItemName(displayName);
    }

    void openBag(Player player, ItemStack item) {
        if (item.getAmount() == 1) {
            player.playSound(player.getEyeLocation(), Sound.ENTITY_HORSE_ARMOR, 1.0f, 0.65f);
            CustomPlugin.getInstance().getInventoryManager().openInventory(player, new BombBagInventory(plugin, player, item));
        } else {
            Msg.sendActionBar(player, "&cUnstack the bomb bag first!");
            player.playSound(player.getEyeLocation(), Sound.BLOCK_DISPENSER_FAIL, 1.0f, 0.55f);
        }
    }

    void updateBagDescription(ItemStack item) {
        Dirty.TagWrapper config = Dirty.TagWrapper.getItemConfigOf(item);
        ItemDescription desc = itemDescription.clone();
        for (CustomTNTType type: CustomTNTType.values()) {
            int amount = config.getInt(type.key);
            if (amount > 0) {
                String tntName = plugin.getItems().get(type).getDisplayName();
                desc.getStats().put(tntName, "" + amount);
            }
        }
        desc.apply(item);
    }
}

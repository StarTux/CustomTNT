package com.winthier.custom_tnt;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.item.CustomItem;
import com.winthier.custom.item.ItemContext;
import com.winthier.custom.item.ItemDescription;
import com.winthier.custom.item.UncraftableItem;
import com.winthier.custom.item.UpdatableItem;
import com.winthier.custom.util.Dirty;
import com.winthier.custom.util.Msg;
import com.winthier.generic_events.GenericEventsPlugin;
import com.winthier.generic_events.ItemNameEvent;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Getter
public final class CustomTNTItem implements CustomItem, UncraftableItem, UpdatableItem {
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
        switch (type) {
        case INCENDIARY:
        case FRAGMENTATION:
        case PRESSURE:
            description.setUsage(plugin.getConfig().getString("lore.BombUsage"));
            break;
        default:
            description.setUsage(plugin.getConfig().getString("lore.Usage"));
            break;
        }
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
    public void onBlockPlace(BlockPlaceEvent event, ItemContext context) {
        switch (type) {
        case INCENDIARY:
        case FRAGMENTATION:
        case PRESSURE:
            break;
        default:
            if (!GenericEventsPlugin.getInstance().playerCanGrief(event.getPlayer(), event.getBlock())) {
                Msg.sendActionBar(event.getPlayer(), noGriefWarning);
                event.setCancelled(true);
                return;
            }
        }
        CustomPlugin.getInstance().getBlockManager().wrapBlock(event.getBlock(), customId);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event, ItemContext context) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR
            || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            switch (type) {
            case INCENDIARY:
            case FRAGMENTATION:
            case PRESSURE:
                ItemStack item = context.getItemStack();
                Player player = context.getPlayer();
                if (player.isSneaking()) return;
                if (!GenericEventsPlugin.getInstance().playerCanBuild(event.getPlayer(), player.getLocation().getBlock())) return;
                event.setCancelled(true);
                CustomTNTEntity.Watcher watcher = (CustomTNTEntity.Watcher)CustomPlugin.getInstance().getEntityManager().spawnEntity(player.getEyeLocation(), customId);
                if (watcher == null) return;
                watcher.setSource(player);
                watcher.getEntity().setVelocity(player.getLocation().getDirection());
                ((TNTPrimed)watcher.getEntity()).setFuseTicks(20);
                if (player.getGameMode() != GameMode.CREATIVE) item.setAmount(item.getAmount() - 1);
                player.playSound(watcher.getEntity().getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0f, 1.8f);
                break;
            default: break;
            }
        }
    }

    @EventHandler
    public void onItemName(ItemNameEvent event) {
        event.setItemName(displayName);
    }

    @Override
    public void updateItem(ItemStack item) {
        itemDescription.apply(item);
    }
}

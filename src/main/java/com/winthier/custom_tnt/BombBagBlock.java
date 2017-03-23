package com.winthier.custom_tnt;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.block.BlockContext;
import com.winthier.custom.block.BlockWatcher;
import com.winthier.custom.block.CustomBlock;
import com.winthier.custom.block.UnbreakableBlock;
import com.winthier.custom.util.Dirty;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@Getter
public final class BombBagBlock implements CustomBlock, UnbreakableBlock {
    private final CustomTNTPlugin plugin;
    private final String customId = "tnt:bomb_bag";

    BombBagBlock(CustomTNTPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setBlock(Block block) {
        block.setType(Material.SKULL);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event, BlockContext context) { }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event, BlockContext context) {
        event.setInstaBreak(true);
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event, BlockContext context) {
        event.setCancelled(true);
        drop(context.getBlockWatcher());
    }

    void drop(BlockWatcher blockWatcher) {
        Object blockData = CustomPlugin.getInstance().getBlockManager().loadBlockData(blockWatcher);
        CustomPlugin.getInstance().getBlockManager().removeBlockWatcher(blockWatcher);
        blockWatcher.getBlock().setType(Material.AIR);
        Item item = CustomPlugin.getInstance().getItemManager().dropItemStack(blockWatcher.getBlock().getLocation().add(0.5, 0.5, 0.5), customId, 1);
        if (blockData != null) {
            Dirty.TagWrapper config = Dirty.TagWrapper.getItemConfigOf(item.getItemStack());
            @SuppressWarnings("unchecked")
            Map<String, Object> blockMap = (Map<String, Object>)blockData;
            for (CustomTNTType type: CustomTNTType.values()) {
                Object value = blockMap.get(type.key);
                if (value != null && value instanceof Number) {
                    config.setInt(type.key, ((Number)value).intValue());
                }
            }
        }
        plugin.getBombBagItem().updateBagDescription(item.getItemStack());
    }

    void save(BlockWatcher watcher, ItemStack item) {
        Map<String, Integer> blockMap = new HashMap<>();
        if (!Dirty.TagWrapper.hasItemConfig(item)) return;
        Dirty.TagWrapper config = Dirty.TagWrapper.getItemConfigOf(item);
        for (CustomTNTType type: CustomTNTType.values()) {
            int value = config.getInt(type.key);
            if (value > 0) {
                blockMap.put(type.key, value);
            }
        }
        CustomPlugin.getInstance().getBlockManager().saveBlockData(watcher, blockMap);
    }
}

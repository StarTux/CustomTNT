package com.winthier.custom_tnt;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.block.BlockContext;
import com.winthier.custom.block.CustomBlock;
import com.winthier.custom.entity.EntityWatcher;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class CustomTNTBlock implements CustomBlock {
    private final CustomTNTPlugin plugin;
    private final CustomTNTType type;
    @Getter private final String customId;

    CustomTNTBlock(CustomTNTPlugin plugin, CustomTNTType type) {
        this.plugin = plugin;
        this.type = type;
        this.customId = type.customId;
    }

    @Override
    public void setBlock(Block block) {
        block.setType(Material.SKULL);
    }

    CustomTNTEntity.Watcher prime(Block block) {
        CustomPlugin.getInstance().getBlockManager().removeBlock(block);
        block.setType(Material.AIR);
        EntityWatcher watcher = CustomPlugin.getInstance().getEntityManager().spawnEntity(block.getLocation().add(0.5, 0.0, 0.5), customId);
        block.getWorld().playSound(block.getLocation().add(0.5, 0.5, 0.5), Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);
        return (CustomTNTEntity.Watcher)watcher;
    }

    void drop(Block block) {
        CustomPlugin.getInstance().getBlockManager().removeBlock(block);
        block.setType(Material.AIR);
        CustomPlugin.getInstance().getItemManager().dropItemStack(block.getLocation().add(0.5, 0.5, 0.5), customId, 1);
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event, BlockContext context) {
        event.setCancelled(true);
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.SHEARS) {
            drop(context.getBlock());
        } else {
            prime(context.getBlock()).setSource(event.getPlayer());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event, BlockContext context) {
        event.setCancelled(true);
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.SHEARS) {
            drop(context.getBlock());
        } else {
            prime(context.getBlock()).setSource(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event, BlockContext context) {
        event.blockList().remove(context.getBlock());
        prime(context.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event, BlockContext context) {
        event.blockList().remove(context.getBlock());
        prime(context.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        event.setCancelled(true);
    }
}

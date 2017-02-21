package com.winthier.custom_tnt;

import com.winthier.custom.CustomConfig;
import com.winthier.custom.CustomPlugin;
import com.winthier.custom.block.AbstractBlockWatcher;
import com.winthier.custom.block.CustomBlock;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
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
    public void setBlock(Block block, CustomConfig config) {
        block.setType(Material.SKULL);
    }

    @Override
    public BlockWatcher createBlockWatcher(Block block, CustomConfig config) {
        return new BlockWatcher(plugin, block, this, config);
    }

    @RequiredArgsConstructor
    public static class BlockWatcher extends AbstractBlockWatcher {
        @Getter private final CustomTNTPlugin plugin;
        @Getter private final Block block;
        @Getter private final CustomTNTBlock customBlock;
        @Getter private final CustomConfig customConfig;

        void prime() {
            CustomPlugin.getInstance().getBlockManager().removeBlock(block);
            block.setType(Material.AIR);
            CustomPlugin.getInstance().getEntityManager().spawnEntity(block.getLocation().add(0.5, 0.0, 0.5), customBlock.getCustomId());
            block.getWorld().playSound(block.getLocation().add(0.5, 0.5, 0.5), Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);
        }

        void drop() {
            CustomPlugin.getInstance().getBlockManager().removeBlock(block);
            block.setType(Material.AIR);
            CustomPlugin.getInstance().getItemManager().dropItemStack(block.getLocation().add(0.5, 0.5, 0.5), customBlock.getCustomId(), 1);
        }

        @EventHandler
        public void onBlockDamage(BlockDamageEvent event) {
            event.setCancelled(true);
            if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.SHEARS) {
                drop();
            } else {
                prime();
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onEntityExplode(EntityExplodeEvent event) {
            event.blockList().remove(block);
        }

        @EventHandler(ignoreCancelled = true)
        public void onBlockExplode(BlockExplodeEvent event) {
            event.blockList().remove(block);
        }

        @EventHandler(ignoreCancelled = true)
        public void onBlockFromTo(BlockFromToEvent event) {
            drop();
        }
    }
}

package com.winthier.custom_tnt;

import com.winthier.custom.CustomConfig;
import com.winthier.custom.CustomPlugin;
import com.winthier.custom.block.AbstractBlockWatcher;
import com.winthier.custom.block.CustomBlock;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;

@RequiredArgsConstructor
public final class CustomTNTBlock implements CustomBlock {
    private final CustomTNTPlugin plugin;
    @Getter private final String customId;

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

        @EventHandler
        public void onBlockDamage(BlockDamageEvent event) {
            event.setCancelled(true);
            block.setType(Material.AIR);
            CustomPlugin.getInstance().getEntityManager().spawnEntity(block.getLocation().add(0.5, 0.0, 0.5), customBlock.getCustomId());
        }
    }
}

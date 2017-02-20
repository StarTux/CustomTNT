package com.winthier.custom_tnt;

import com.winthier.custom.CustomConfig;
import com.winthier.custom.entity.AbstractEntityWatcher;
import com.winthier.custom.entity.CustomEntity;
import com.winthier.custom.entity.EntityWatcher;
import com.winthier.ore.OrePlugin;
import java.util.Iterator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class CustomTNTEntity implements CustomEntity {
    private final CustomTNTPlugin plugin;
    private final CustomTNTType type;
    @Getter private final String customId;

    CustomTNTEntity(CustomTNTPlugin plugin, CustomTNTType type) {
        this.plugin = plugin;
        this.type = type;
        this.customId = type.customId;
    }

    @Override
    public Entity spawnEntity(Location location, CustomConfig config) {
        TNTPrimed entity = location.getWorld().spawn(location, TNTPrimed.class);
        entity.setIsIncendiary(false);
        entity.setYield(type.yield);
        return entity;
    }

    @Override
    public EntityWatcher createEntityWatcher(Entity entity, CustomConfig config) {
        TNTPrimed tnt = entity instanceof TNTPrimed ? (TNTPrimed)entity : null;
        if (tnt == null) return null;
        return new CustomTNTEntityWatcher(plugin, tnt, this, config, type);
    }

    @RequiredArgsConstructor
    public static final class CustomTNTEntityWatcher extends AbstractEntityWatcher {
        @Getter private final CustomTNTPlugin plugin;
        @Getter private final TNTPrimed entity;
        @Getter private final CustomEntity customEntity;
        @Getter private final CustomConfig customConfig;
        private final CustomTNTType type;

        @EventHandler(ignoreCancelled = true)
        public void onEntityExplode(EntityExplodeEvent event) {
            Iterator<Block> iter = event.blockList().iterator();
            while (iter.hasNext()) {
                Block block = iter.next();
                OrePlugin.getInstance().realizeBlock(block);
                switch (type) {
                case MINING:
                    filterMining(iter, block);
                    break;
                case WOODCUTTING:
                    filterWoodcutting(iter, block);
                    break;
                default:
                    plugin.getLogger().warning("Unhandled TNT type: " + type);
                    iter.remove();
                    break;
                }
            }
        }

        void filterMining(Iterator<Block> iter, Block block) {
            switch (block.getType()) {
            case STONE:
            case DIRT:
            case GRASS:
            case GRAVEL:
            case SAND:
                break;
            default:
                iter.remove();
            }
        }

        void filterWoodcutting(Iterator<Block> iter, Block block) {
            switch (block.getType()) {
            case LOG:
            case LOG_2:
            case LEAVES:
            case LEAVES_2:
            case HUGE_MUSHROOM_1:
            case HUGE_MUSHROOM_2:
                break;
            default:
                iter.remove();
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
            event.setCancelled(true);
        }
    }
}

package com.winthier.custom_tnt;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.block.BlockWatcher;
import com.winthier.custom.entity.CustomEntity;
import com.winthier.custom.entity.EntityContext;
import com.winthier.custom.entity.EntityWatcher;
import com.winthier.ore.OrePlugin;
import java.util.Iterator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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
    public Entity spawnEntity(Location location) {
        TNTPrimed entity = location.getWorld().spawn(location, TNTPrimed.class);
        entity.setIsIncendiary(false);
        entity.setYield(type.yield);
        return entity;
    }

    @Override
    public EntityWatcher createEntityWatcher(Entity entity) {
        TNTPrimed tnt = entity instanceof TNTPrimed ? (TNTPrimed)entity : null;
        if (tnt == null) return null;
        return new Watcher(plugin, tnt, this, type);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event, EntityContext context) {
        CustomPlugin.getInstance().getEntityManager().removeEntity(context.getEntityWatcher());
        Iterator<Block> iter = event.blockList().iterator();
        while (iter.hasNext()) {
            Block block = iter.next();
            BlockWatcher foundWatcher = CustomPlugin.getInstance().getBlockManager().getBlockWatcher(block);
            if (foundWatcher != null) {
                if (!(foundWatcher.getCustomBlock() instanceof CustomTNTBlock)) {
                    iter.remove();
                }
            } else {
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

    @RequiredArgsConstructor
    public static final class Watcher implements EntityWatcher {
        @Getter private final CustomTNTPlugin plugin;
        @Getter private final TNTPrimed entity;
        @Getter private final CustomEntity customEntity;
        private final CustomTNTType type;
        @Getter @Setter Player source;
    }
}

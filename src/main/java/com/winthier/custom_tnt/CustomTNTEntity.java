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
import org.bukkit.event.entity.EntityExplodeEvent;

@RequiredArgsConstructor
public final class CustomTNTEntity implements CustomEntity {
    private final CustomTNTPlugin plugin;
    @Getter private final String customId;

    @Override
    public Entity spawnEntity(Location location, CustomConfig config) {
        TNTPrimed entity = location.getWorld().spawn(location, TNTPrimed.class);
        entity.setIsIncendiary(false);
        entity.setYield(16.0f);
        return entity;
    }

    @Override
    public EntityWatcher createEntityWatcher(Entity entity, CustomConfig config) {
        TNTPrimed tnt = entity instanceof TNTPrimed ? (TNTPrimed)entity : null;
        if (tnt == null) return null;
        return new CustomTNTEntityWatcher(plugin, tnt, this, config);
    }

    @RequiredArgsConstructor
    public static final class CustomTNTEntityWatcher extends AbstractEntityWatcher {
        @Getter private final CustomTNTPlugin plugin;
        @Getter private final TNTPrimed entity;
        @Getter private final CustomEntity customEntity;
        @Getter private final CustomConfig customConfig;

        @EventHandler(ignoreCancelled = true)
        public void onEntityExplode(EntityExplodeEvent event) {
            Iterator<Block> iter = event.blockList().iterator();
            while (iter.hasNext()) {
                Block block = iter.next();
                OrePlugin.getInstance().realizeBlock(block);
                switch (block.getType()) {
                case STONE:
                case DIRT:
                case GRASS:
                    break;
                default:
                    iter.remove();
                }
            }
        }
    }
}

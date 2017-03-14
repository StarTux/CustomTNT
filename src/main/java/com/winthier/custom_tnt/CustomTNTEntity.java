package com.winthier.custom_tnt;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.block.BlockWatcher;
import com.winthier.custom.entity.CustomEntity;
import com.winthier.custom.entity.EntityContext;
import com.winthier.custom.entity.EntityWatcher;
import com.winthier.generic_events.GenericEventsPlugin;
import com.winthier.ore.OrePlugin;
import java.util.Iterator;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public final class CustomTNTEntity implements CustomEntity {
    private final CustomTNTPlugin plugin;
    private final CustomTNTType type;
    @Getter private final String customId;
    private final Random random = new Random(System.currentTimeMillis());

    CustomTNTEntity(CustomTNTPlugin plugin, CustomTNTType type) {
        this.plugin = plugin;
        this.type = type;
        this.customId = type.customId;
    }

    @Override
    public Entity spawnEntity(Location location) {
        TNTPrimed entity = location.getWorld().spawn(location, TNTPrimed.class);
        entity.setIsIncendiary(false);
        float yield = (float)plugin.getConfig().getConfigurationSection("types").getConfigurationSection(type.key).getDouble("Yield");
        entity.setYield(yield);
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
        Player player = ((Watcher)context.getEntityWatcher()).getSource();
        while (iter.hasNext()) {
            Block block = iter.next();
            if (!GenericEventsPlugin.getInstance().playerCanBuild(player, block)
                || !GenericEventsPlugin.getInstance().playerCanGrief(player, block)) {
                iter.remove();
                continue;
            }
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
                case NUKE:
                    filterNuke(iter, block);
                    break;
                case SILK:
                    filterSilk(iter, block);
                    break;
                case KINETIC:
                    filterKinetic(iter, block);
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

    void filterNuke(Iterator<Block> iter, Block block) {
        switch (block.getType()) {
        case LEAVES:
        case LEAVES_2:
        case GLASS:
        case THIN_GLASS:
        case STAINED_GLASS:
        case STAINED_GLASS_PANE:
        case DOUBLE_PLANT:
        case ICE:
        case BROWN_MUSHROOM:
        case RED_MUSHROOM:
        case LONG_GRASS:
        case DEAD_BUSH:
        case VINE:
            break;
        case YELLOW_FLOWER:
        case RED_ROSE:
            iter.remove();
            block.setType(Material.DEAD_BUSH);
            break;
        case GRASS:
        case MYCEL:
            block.setType(Material.DIRT);
            iter.remove();
            break;
        default:
            iter.remove();
        }
    }

    void filterSilk(Iterator<Block> iter, Block block) {
        Material mat = block.getType();
        switch (mat) {
        case STONE:
        case COAL_ORE:
        case DIAMOND_ORE:
        case EMERALD_ORE:
        case REDSTONE_ORE:
        case GOLD_ORE:
        case IRON_ORE:
        case LAPIS_ORE:
        case QUARTZ_ORE:
        case GRASS:
        case WEB:
        case GLASS:
        case THIN_GLASS:
        case ICE:
        case VINE:
            iter.remove();
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(mat));
            break;
        case STAINED_GLASS:
        case STAINED_GLASS_PANE:
            byte data = block.getData();
            iter.remove();
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(mat, 1, data));
            break;
        case GLOWING_REDSTONE_ORE:
            iter.remove();
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.REDSTONE_ORE));
            break;
        default:
            iter.remove();
        }
    }

    void filterKinetic(Iterator<Block> iter, Block block) {
        iter.remove();
        MaterialData data = block.getState().getData();
        block.setType(Material.AIR);
        Vector velo = new Vector(random.nextDouble() * 2.0 - 1.0,
                                 random.nextDouble() * 2.0,
                                 random.nextDouble() * 2.0 - 1.0);
        block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 0.0, 0.5), data).setVelocity(velo);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        event.setCancelled(true);
    }

    @Getter @RequiredArgsConstructor
    public static final class Watcher implements EntityWatcher {
        private final CustomTNTPlugin plugin;
        private final TNTPrimed entity;
        private final CustomEntity customEntity;
        private final CustomTNTType type;
        @Setter private Player source;
    }
}

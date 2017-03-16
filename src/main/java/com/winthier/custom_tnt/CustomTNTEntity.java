package com.winthier.custom_tnt;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.block.BlockWatcher;
import com.winthier.custom.entity.CustomEntity;
import com.winthier.custom.entity.EntityContext;
import com.winthier.custom.entity.EntityWatcher;
import com.winthier.generic_events.GenericEventsPlugin;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class CustomTNTEntity implements CustomEntity {
    private final CustomTNTPlugin plugin;
    private final CustomTNTType type;
    @Getter private final String customId;
    private final Random random = new Random(System.currentTimeMillis());
    private final float yield;

    CustomTNTEntity(CustomTNTPlugin plugin, CustomTNTType type) {
        this.plugin = plugin;
        this.type = type;
        this.customId = type.customId;
        this.yield = (float)plugin.getConfig().getConfigurationSection("types").getConfigurationSection(type.key).getDouble("Yield");
    }

    @Override
    public Entity spawnEntity(Location location) {
        TNTPrimed entity = location.getWorld().spawn(location, TNTPrimed.class);
        entity.setIsIncendiary(false);
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
        CustomPlugin.getInstance().getEntityManager().removeEntityWatcher(context.getEntityWatcher());
        Iterator<Block> iter = event.blockList().iterator();
        Player player = ((Watcher)context.getEntityWatcher()).getSource();
        if (player == null) {
            event.setCancelled(true);
            event.blockList().clear();
            return;
        }
        List<Block> customExplodeBlocks = new ArrayList<>();
        while (iter.hasNext()) {
            Block block = iter.next();
            if (!GenericEventsPlugin.getInstance().playerCanBuild(player, block)
                || !GenericEventsPlugin.getInstance().playerCanGrief(player, block)) {
                iter.remove();
                continue;
            }
            BlockWatcher foundWatcher = CustomPlugin.getInstance().getBlockManager().getBlockWatcher(block);
            if (foundWatcher != null) {
                if (foundWatcher.getCustomBlock() instanceof CustomTNTBlock) {
                    CustomTNTBlock customTNTBlock = (CustomTNTBlock)foundWatcher.getCustomBlock();
                    customTNTBlock.prime(foundWatcher, player);
                    customExplodeBlocks.add(block);
                }
                iter.remove();
            } else {
                switch (type) {
                case MINING:
                    filterMining(iter, block);
                    break;
                case WOODCUTTING:
                    filterWoodcutting(iter, block);
                    break;
                case NUKE:
                    filterNuke(iter, block, customExplodeBlocks);
                    break;
                case SILK:
                    filterSilk(iter, block, customExplodeBlocks);
                    break;
                case KINETIC:
                    filterKinetic(iter, block, customExplodeBlocks);
                    break;
                case POWER:
                    break;
                default:
                    plugin.getLogger().warning("Unhandled TNT type: " + type);
                    iter.remove();
                    break;
                }
            }
        }
        if (!customExplodeBlocks.isEmpty()) {
            EntityExplodeEvent customEvent = new EntityExplodeEvent(event.getEntity(), event.getEntity().getLocation(), customExplodeBlocks, yield);
            plugin.getServer().getPluginManager().callEvent(customEvent);
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

    void filterNuke(Iterator<Block> iter, Block block, List<Block> customExplodeBlocks) {
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
        case WEB:
        case CROPS:
        case CARROT:
        case POTATO:
        case NETHER_STALK:
        case BEETROOT_BLOCK:
        case WATER_LILY:
        case CACTUS:
        case CHORUS_FLOWER:
        case CHORUS_PLANT:
        case SUGAR_CANE_BLOCK:
        case HAY_BLOCK:
        case PUMPKIN:
        case JACK_O_LANTERN:
        case MELON_BLOCK:
        case SNOW:
        case COCOA:
            break;
        case YELLOW_FLOWER:
        case RED_ROSE:
        case SAPLING:
        case MELON_STEM:
        case PUMPKIN_STEM:
            block.setType(Material.DEAD_BUSH);
            iter.remove();
            customExplodeBlocks.add(block);
            break;
        case GRASS:
        case MYCEL:
        case DIRT:
        case GRASS_PATH:
            iter.remove();
            block.setType(Material.DIRT);
            block.setData((byte)1); // Coarse dirt
            customExplodeBlocks.add(block);
            break;
        case SOIL:
            iter.remove();
            block.setType(Material.SOUL_SAND);
            customExplodeBlocks.add(block);
            break;
        case SAND:
            iter.remove();
            block.setType(Material.GLASS);
            customExplodeBlocks.add(block);
            break;
        default:
            iter.remove();
        }
    }

    void filterSilk(Iterator<Block> iter, Block block, List<Block> customExplodeBlocks) {
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
        case MYCEL:
        case WEB:
        case GLASS:
        case THIN_GLASS:
        case ICE:
        case VINE:
        case SNOW:
            // Copy without data
            iter.remove();
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(mat));
            customExplodeBlocks.add(block);
            break;
        case STAINED_GLASS:
        case STAINED_GLASS_PANE:
        case LONG_GRASS:
            // Copy with data
            iter.remove();
            byte data = block.getData();
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(mat, 1, data));
            customExplodeBlocks.add(block);
            break;
        case LEAVES:
        case LEAVES_2:
            // Copy with modified leaf data
            iter.remove();
            int iData = (int)block.getData() & ~12;
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(mat, 1, (byte)iData));
            customExplodeBlocks.add(block);
            break;
        case DIRT:
            // Copy on condition
            iter.remove();
            data = block.getData();
            if (data != 0) {
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(mat, 1, data));
                customExplodeBlocks.add(block);
            }
            break;
        case GLOWING_REDSTONE_ORE:
            iter.remove();
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.REDSTONE_ORE));
            customExplodeBlocks.add(block);
            break;
        default:
            iter.remove();
        }
    }

    void filterKinetic(Iterator<Block> iter, Block block, List<Block> customExplodeBlocks) {
        switch (block.getType()) {
        case MOB_SPAWNER:
            return;
        default:
            break;
        }
        iter.remove();
        MaterialData data = block.getState().getData();
        block.setType(Material.AIR);
        Vector velo = new Vector(random.nextDouble() * 0.5 - 0.25,
                                 random.nextDouble() * 2.0 + 1.0,
                                 random.nextDouble() * 0.5 - 0.25);
        block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 0.0, 0.5), data).setVelocity(velo);
        customExplodeBlocks.add(block);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event, EntityContext context) {
        event.setCancelled(true);
        if (context.getPosition() == EntityContext.Position.DAMAGER
            && event.getFinalDamage() > 1.0
            && event.getEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity)event.getEntity();
            Player player = ((Watcher)context.getEntityWatcher()).getSource();
            if (GenericEventsPlugin.getInstance().playerCanDamageEntity(player, entity)) {
                switch (type) {
                case NUKE:
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 60, 1));
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * 60, 1));
                    if (entity instanceof Sheep) ((Sheep)entity).setSheared(true);
                    if (entity instanceof Creeper) ((Creeper)entity).setPowered(true);
                    break;
                case KINETIC:
                    entity.setVelocity(new Vector(random.nextDouble() * 1.0 - 0.5,
                                                  random.nextDouble() * 4.0 + 2.0,
                                                  random.nextDouble() * 1.0 - 0.5));
                    break;
                default:
                    break;
                }
            }
        }
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

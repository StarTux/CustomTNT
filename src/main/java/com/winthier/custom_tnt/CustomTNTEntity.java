package com.winthier.custom_tnt;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.block.BlockWatcher;
import com.winthier.custom.entity.CustomEntity;
import com.winthier.custom.entity.EntityContext;
import com.winthier.custom.entity.EntityWatcher;
import com.winthier.generic_events.GenericEventsPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
    private static final MaterialData AIR = new MaterialData(Material.AIR);
    private boolean ignoreEntityExplodeEvent;

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
        if (ignoreEntityExplodeEvent) return;
        CustomPlugin.getInstance().getEntityManager().removeEntityWatcher(context.getEntityWatcher());
        Player player = ((Watcher)context.getEntityWatcher()).getSource();
        if (player == null) {
            event.setCancelled(true);
            event.blockList().clear();
            return;
        }
        Entity tnt = context.getEntity();
        for (Entity damagee: tnt.getNearbyEntities(yield, yield, yield)) {
            if (damagee instanceof Hanging) continue;
            hitEntity(player, damagee);
        }
        switch (type) { // Special case bombs
        case FRAGMENTATION:
            event.blockList().clear();
            for (int i = 0; i < 100; i += 1) {
                EntityWatcher watcher = CustomPlugin.getInstance().getEntityManager().spawnEntity(tnt.getLocation(), "tnt:shrapnel");
                ((Arrow)watcher.getEntity()).setShooter(player);
            }
            return;
        case PRESSURE:
        case INCENDIARY:
            // Do nothing, wait for entity damage
            event.blockList().clear();
            return;
        default: break;
        }
        Map<Block, MaterialData> customExplodeBlocks = new HashMap<>();
        Iterator<Block> iter = event.blockList().iterator();
        while (iter.hasNext()) {
            Block block = iter.next();
            if (!GenericEventsPlugin.getInstance().playerCanGrief(player, block)) {
                iter.remove();
                continue;
            }
            BlockWatcher foundWatcher = CustomPlugin.getInstance().getBlockManager().getBlockWatcher(block);
            if (foundWatcher != null) {
                if (foundWatcher.getCustomBlock() instanceof CustomTNTBlock) {
                    CustomTNTBlock customTNTBlock = (CustomTNTBlock)foundWatcher.getCustomBlock();
                    customTNTBlock.prime(foundWatcher, player);
                }
                iter.remove();
            } else {
                switch (type) {
                case MINING:
                    filterMining(iter, block);
                    break;
                case WOODCUTTING:
                    filterWoodcutting(iter, block, customExplodeBlocks);
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
            List<Block> newBlockList = new ArrayList<Block>(customExplodeBlocks.keySet());
            EntityExplodeEvent customEvent = new EntityExplodeEvent(event.getEntity(), event.getEntity().getLocation(), newBlockList, yield);
            ignoreEntityExplodeEvent = true;
            plugin.getServer().getPluginManager().callEvent(customEvent);
            ignoreEntityExplodeEvent = false;
            if (customEvent.isCancelled()) return;
            for (Block block: newBlockList) {
                MaterialData data = customExplodeBlocks.get(block);
                switch (type) {
                case WOODCUTTING:
                    block.breakNaturally();
                    break;
                case SILK:
                    block.setType(Material.AIR);
                    block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(data.getItemType(), 1, data.getData()));
                    break;
                case KINETIC:
                    block.setType(Material.AIR);
                    Vector velo = new Vector(random.nextDouble() * 0.5 - 0.25,
                                             random.nextDouble() * 2.0 + 1.0,
                                             random.nextDouble() * 0.5 - 0.25);
                    FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 0.0, 0.5), data);
                    falling.setVelocity(velo);
                    falling.setDropItem(false);
                    break;
                default:
                    block.setTypeIdAndData(data.getItemType().getId(), data.getData(), true);
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

    void filterWoodcutting(Iterator<Block> iter, Block block, Map<Block, MaterialData> customExplodeBlocks) {
        iter.remove();
        switch (block.getType()) {
        case LOG:
        case LOG_2:
        case LEAVES:
        case LEAVES_2:
        case HUGE_MUSHROOM_1:
        case HUGE_MUSHROOM_2:
            customExplodeBlocks.put(block, AIR);
            break;
        default:
            break;
        }
    }

    void filterNuke(Iterator<Block> iter, Block block, Map<Block, MaterialData> customExplodeBlocks) {
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
            iter.remove();
            customExplodeBlocks.put(block, new MaterialData(Material.DEAD_BUSH));
            break;
        case GRASS:
        case MYCEL:
        case DIRT:
        case GRASS_PATH:
            iter.remove();
            // Coarse dirt
            customExplodeBlocks.put(block, new MaterialData(Material.DIRT, (byte)1));
            break;
        case SOIL:
            iter.remove();
            customExplodeBlocks.put(block, new MaterialData(Material.SOUL_SAND));
            break;
        case SAND:
            iter.remove();
            customExplodeBlocks.put(block, new MaterialData(Material.GLASS));
            break;
        default:
            iter.remove();
        }
    }

    void filterSilk(Iterator<Block> iter, Block block, Map<Block, MaterialData> customExplodeBlocks) {
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
        case PACKED_ICE:
        case VINE:
        case SNOW:
        case SEA_LANTERN:
        case HUGE_MUSHROOM_1:
        case HUGE_MUSHROOM_2:
        case ENDER_CHEST:
        case BOOKSHELF:
            // Copy without data
            iter.remove();
            customExplodeBlocks.put(block, new MaterialData(mat));
            break;
        case STAINED_GLASS:
        case STAINED_GLASS_PANE:
        case LONG_GRASS:
            // Copy with data
            iter.remove();
            byte data = block.getData();
            customExplodeBlocks.put(block, new MaterialData(mat, data));
            break;
        case LEAVES:
        case LEAVES_2:
            // Copy with modified leaf data
            iter.remove();
            int iData = (int)block.getData() & ~12;
            customExplodeBlocks.put(block, new MaterialData(mat, (byte)iData));
            break;
        case DIRT:
            // Copy on condition
            iter.remove();
            data = block.getData();
            if (data != 0) {
                customExplodeBlocks.put(block, new MaterialData(mat, data));
            }
            break;
        case GLOWING_REDSTONE_ORE:
            // Copy modified material
            iter.remove();
            customExplodeBlocks.put(block, new MaterialData(Material.REDSTONE_ORE));
            break;
        default:
            iter.remove();
        }
    }

    void filterKinetic(Iterator<Block> iter, Block block, Map<Block, MaterialData> customExplodeBlocks) {
        switch (block.getType()) {
        case MOB_SPAWNER:
        case WALL_SIGN:
        case SIGN_POST:
        case STANDING_BANNER:
        case WALL_BANNER:
        case SKULL:
        case TNT:
            return;
        default:
            break;
        }
        BlockState blockState = block.getState();
        if (blockState instanceof org.bukkit.block.Chest) return;
        if (blockState instanceof org.bukkit.block.ShulkerBox) return;
        if (blockState instanceof org.bukkit.block.Skull) return;
        if (blockState instanceof org.bukkit.block.Banner) return;
        if (blockState instanceof org.bukkit.block.CommandBlock) return;
        if (blockState instanceof org.bukkit.block.CreatureSpawner) return;
        iter.remove();
        customExplodeBlocks.put(block, block.getState().getData());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event, EntityContext context) {
        event.setCancelled(true);
    }

    void hitEntity(Player player, Entity entity) {
        if (!GenericEventsPlugin.getInstance().playerCanDamageEntity(player, entity)) return;
        LivingEntity living = entity instanceof LivingEntity ? (LivingEntity)entity : null;
        switch (type) {
        case INCENDIARY:
        case FRAGMENTATION:
        case PRESSURE:
            break;
        default:
            if (!GenericEventsPlugin.getInstance().playerCanGrief(player, entity.getLocation().getBlock())) return;
        }
        switch (type) {
        case NUKE:
            if (living != null) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 60, 1));
                living.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * 60, 1));
                if (living instanceof Sheep) ((Sheep)living).setSheared(true);
                if (living instanceof Creeper) ((Creeper)living).setPowered(true);
            }
            break;
        case KINETIC:
        case PRESSURE:
            entity.setVelocity(new Vector(random.nextDouble() * 1.0 - 0.5,
                                          random.nextDouble() * 4.0 + 2.0,
                                          random.nextDouble() * 1.0 - 0.5));
            break;
        case INCENDIARY:
            entity.setFireTicks(Math.max(entity.getFireTicks(), 20 * 60));
        default:
            break;
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

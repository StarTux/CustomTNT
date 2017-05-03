package com.winthier.custom_tnt;

import com.winthier.custom.entity.CustomEntity;
import com.winthier.custom.entity.EntityContext;
import com.winthier.custom.entity.EntityWatcher;
import com.winthier.generic_events.GenericEventsPlugin;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

@Getter @RequiredArgsConstructor
public class Shrapnel implements CustomEntity {
    private final CustomTNTPlugin plugin;
    private final Random random = new Random(System.currentTimeMillis());
    private final String customId = "tnt:shrapnel";

    @Override
    public Arrow spawnEntity(Location location) {
        return location.getWorld().spawn(location, Arrow.class, new Consumer<Arrow>() {
            @Override public void accept(Arrow arrow) {
                arrow.setVelocity(new Vector(random.nextDouble() * 2.0 - 1.0,
                                             random.nextDouble() * 0.5,
                                             random.nextDouble() * 2.0 - 1.0));
                arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                arrow.setFireTicks(100);
                arrow.setKnockbackStrength(2);
            }
        });
    }

    @Override
    public Watcher createEntityWatcher(Entity entity) {
        return new Watcher((Arrow)entity, this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onProjectileHit(ProjectileHitEvent event, EntityContext context) {
        if (context.getPosition() != EntityContext.Position.ENTITY) return;
        Arrow arrow = (Arrow)event.getEntity();
        Player player = ((Watcher)context.getEntityWatcher()).getShooter();
        if (player == null) {
            arrow.remove();
        } else {
            if (event.getHitEntity() != null) {
                if (!GenericEventsPlugin.getInstance().playerCanDamageEntity(player, event.getHitEntity())) {
                    arrow.remove();
                } else if (event.getHitEntity() instanceof Hanging) {
                    arrow.remove();
                }
            }
            if (event.getHitBlock() != null) {
                arrow.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event, EntityContext context) {
        if (context.getPosition() != EntityContext.Position.DAMAGER) return;
        Arrow arrow = (Arrow)event.getDamager();
        Player player = ((Watcher)context.getEntityWatcher()).getShooter();
        if (player == null) {
            event.setCancelled(true);
            arrow.remove();
        } else {
            if (!GenericEventsPlugin.getInstance().playerCanDamageEntity(player, event.getEntity())) {
                event.setCancelled(true);
                arrow.remove();
            } else {
                event.setDamage(20.0);
            }
        }
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        event.setCancelled(true);
    }

    @Getter @RequiredArgsConstructor
    class Watcher implements EntityWatcher {
        private final Arrow entity;
        private final Shrapnel customEntity;
        @Setter private Player shooter;
    }
}

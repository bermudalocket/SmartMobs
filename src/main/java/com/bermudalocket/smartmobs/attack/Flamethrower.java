package com.bermudalocket.smartmobs.attack;

import com.bermudalocket.smartmobs.SmartMobs;
import com.bermudalocket.smartmobs.Util;
import com.bermudalocket.smartmobs.Zone;
import com.bermudalocket.smartmobs.ZoneHandler;
import com.bermudalocket.smartmobs.attack.module.Randomizable;
import com.bermudalocket.smartmobs.attack.module.Reinforceable;
import com.bermudalocket.smartmobs.attack.module.ModuleType;
import de.slikey.effectlib.effect.LineEffect;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Flamethrower extends AbstractSpecialAttack {

    public Flamethrower() {
        super("flamethrower", ProjectileLaunchEvent.class, new Randomizable(), new Reinforceable());
    }

    @Override
    boolean checkConditions(Event event) {
        ProjectileLaunchEvent e = (ProjectileLaunchEvent) event;
        if (e.getEntity() != null) {
            return e.getEntity().getShooter() instanceof Blaze;
        }
        return false;
    }

    @Override
    void doAttack(Event event) {
        ProjectileLaunchEvent e = (ProjectileLaunchEvent) event;
        Blaze blaze = (Blaze) e.getEntity().getShooter();
        Projectile projectile = e.getEntity();
        Zone zone = ZoneHandler.getZone(blaze.getLocation());

        LivingEntity livingEntity = blaze.getTarget();
        if (livingEntity == null) {
            return;
        }

        Randomizable randomizable = (Randomizable) getModule(ModuleType.RANDOMIZABLE);
        Reinforceable reinforceable = (Reinforceable) getModule(ModuleType.REINFORCEABLE);
        if (randomizable.randomize(zone)) {
            Long lastAttacked = _lastAttacked.get(blaze.getUniqueId());
            if (lastAttacked != null) {
                if (System.currentTimeMillis() - lastAttacked < 1000 * 10) {
                    return;
                }
            }
            projectile.remove();
            //e.setCancelled(true);
            int min = reinforceable.getMinReinforcements(zone);
            int max = reinforceable.getMaxReinforcements(zone);
            renderFlamethrower(blaze, livingEntity, Util.random(min, max));
            _lastAttacked.put(blaze.getUniqueId(), System.currentTimeMillis());
        }
    }

    private void renderFlamethrower(Entity origin, Entity target, int duration) {
        LineEffect lineEffect = new LineEffect(SmartMobs.EFFECT_MANAGER);
        lineEffect.setEntity(origin);
        lineEffect.setTargetEntity(target);
        lineEffect.particle = Particle.FLAME;
        lineEffect.disappearWithOriginEntity = true;
        lineEffect.disappearWithTargetEntity = true;
        lineEffect.duration = Util.TPS * duration * 100;
        lineEffect.particleCount = 1;
        lineEffect.start();
        for (int i = 0; i < duration; i++) {
            Runnable hurtTarget = () -> target.setFireTicks((int) (Util.TPS * duration * 1.5));
            Bukkit.getScheduler().runTaskLater(SmartMobs.PLUGIN, hurtTarget, i*Util.TPS);
        }
    }

    @EventHandler
    protected void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof Blaze) {
            _lastAttacked.remove(e.getEntity().getUniqueId());
        }
    }

    private Map<UUID, Long> _lastAttacked = new HashMap<>();

}

package com.bermudalocket.smartmobs.attack;

import com.bermudalocket.smartmobs.SmartMobs;
import com.bermudalocket.smartmobs.Util;
import com.bermudalocket.smartmobs.Zone;
import com.bermudalocket.smartmobs.ZoneHandler;
import com.bermudalocket.smartmobs.attack.module.ModuleType;
import com.bermudalocket.smartmobs.attack.module.Randomizable;
import com.bermudalocket.smartmobs.attack.module.Reinforceable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * EnderDance.
 *
 * An Enderman "dances" around a player after being attacked, summoning Endermite reinforcements and releasing
 * a smokescreen.
 */
public class EnderDance extends AbstractSpecialAttack {

    private static final Set<EntityType> SUPPORTED_MOBS = new HashSet<>(Collections.singletonList(
            EntityType.ENDERMAN
    ));

    public EnderDance() {
        super("enderdance", EntityDamageByEntityEvent.class, new Randomizable(), new Reinforceable());
    }

    @Override
    boolean checkConditions(Event event) {
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
        LivingEntity livingEntity = Util.parseEntityDamageByEntityEvent(e);
        return checkDefaultConditions(e, livingEntity, SUPPORTED_MOBS);
    }

    @Override
    void doAttack(Event event) {
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
        Enderman enderman = (Enderman) Util.parseEntityDamageByEntityEvent(e);
        Location loc = enderman.getLocation();
        World world = loc.getWorld();
        Zone zone = ZoneHandler.getZone(loc);

        Player player = (e.getDamager() instanceof Player) ? (Player) e.getDamager() : null;

        Randomizable randomizable = (Randomizable) getModule(ModuleType.RANDOMIZABLE);
        Reinforceable reinforceable = (Reinforceable) getModule(ModuleType.REINFORCEABLE);

        if (randomizable.randomize(zone)) {
            int min = reinforceable.getMinReinforcements(zone);
            int max = reinforceable.getMaxReinforcements(zone);
            int n = Util.random(min, max);

            for (int i = 0; i < n; i++) {
                Runnable teleportRunnable = () -> {
                    Endermite endermite = (Endermite) world.spawnEntity(loc, EntityType.ENDERMITE);
                    world.spawnParticle(Particle.SMOKE_NORMAL, loc, 50);
                    world.playSound(enderman.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
                    endermite.setTarget(player);
                };
                Bukkit.getScheduler().runTaskLater(SmartMobs.PLUGIN, teleportRunnable, i*10);
            }

        }
    }

}

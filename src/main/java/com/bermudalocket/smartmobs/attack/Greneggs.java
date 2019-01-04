package com.bermudalocket.smartmobs.attack;

import com.bermudalocket.smartmobs.SmartMobs;
import com.bermudalocket.smartmobs.Util;
import com.bermudalocket.smartmobs.Zone;
import com.bermudalocket.smartmobs.ZoneHandler;
import com.bermudalocket.smartmobs.attack.module.Randomizable;
import com.bermudalocket.smartmobs.attack.module.Reinforceable;
import com.bermudalocket.smartmobs.attack.module.ModuleType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// ----------------------------------------------------------------------------------------------------------
/**
 * Greneggs.
 *
 * Upon being attacked, spiders and cave spiders may be so startled that they lay an egg. Might not want to
 * stick around too long, though... who knows what will hatch.
 */
public final class Greneggs extends AbstractSpecialAttack {

    // ------------------------------------------------------------------------------------------------------
    /**
     * The supported EntityTypes.
     */
    private static final Set<EntityType> SUPPORTED_MOBS = new HashSet<>(Arrays.asList(
            EntityType.SPIDER, EntityType.CAVE_SPIDER
    ));

    // ------------------------------------------------------------------------------------------------------
    /**
     * Constructor.
     */
    public Greneggs() {
        super("greneggs", EntityDamageByEntityEvent.class, new Randomizable(), new Reinforceable());
    }

    protected boolean checkConditions(Event event) {
        LivingEntity e = Util.parseEntityDamageByEntityEvent((EntityDamageByEntityEvent) event);
        return checkDefaultConditions((EntityDamageByEntityEvent) event, e, SUPPORTED_MOBS);
    }

    @Override
    void doAttack(Event event) {
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
        LivingEntity entity = Util.parseEntityDamageByEntityEvent(e);
        Location loc = entity.getLocation();
        Zone zone = ZoneHandler.getZone(loc);
        Randomizable randomizable = (Randomizable) getModule(ModuleType.RANDOMIZABLE);

        double probability = randomizable.getProbability(zone);
        if (Util.random(0.0, 1.0) > probability) {
            return;
        }

        World world = loc.getWorld();
        Item egg = world.dropItemNaturally(loc, new ItemStack(Material.EGG));
        egg.setPickupDelay(Integer.MAX_VALUE);

        tickEggBomb(egg);
    }

    private void tickEggBomb(Item egg) {
        final Location loc = egg.getLocation();
        final Zone zone = ZoneHandler.getZone(loc);

        Reinforceable reinforceable = (Reinforceable) getModule(ModuleType.REINFORCEABLE);

        Runnable tick = () -> {
            egg.setGlowing(true);
            egg.getWorld().playSound(egg.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            Bukkit.getScheduler().runTaskLater(SmartMobs.PLUGIN, () -> egg.setGlowing(false), 3);
        };
        Runnable explode = () -> {
            egg.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0.25f, false, true);
            egg.remove();
            int n = Util.random(reinforceable.getMinReinforcements(zone), reinforceable.getMaxReinforcements(zone));
            for (int i = 0; i < n; i++) {
                Util.summonEntityWithVelocity(loc, EntityType.SPIDER);
            }
        };

        Bukkit.getScheduler().runTaskLater(SmartMobs.PLUGIN, tick, Util.TPS);
        Bukkit.getScheduler().runTaskLater(SmartMobs.PLUGIN, tick, 2*Util.TPS);
        Bukkit.getScheduler().runTaskLater(SmartMobs.PLUGIN, tick, 3*Util.TPS);
        Bukkit.getScheduler().runTaskLater(SmartMobs.PLUGIN, tick, 3*Util.TPS + 5);
        Bukkit.getScheduler().runTaskLater(SmartMobs.PLUGIN, tick, 3*Util.TPS + 10);
        Bukkit.getScheduler().runTaskLater(SmartMobs.PLUGIN, tick, 3*Util.TPS + 15);
        Bukkit.getScheduler().runTaskLater(SmartMobs.PLUGIN, explode, 4*Util.TPS);
    }

}
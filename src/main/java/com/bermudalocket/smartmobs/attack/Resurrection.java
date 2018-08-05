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
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// ----------------------------------------------------------------------------------------------------------
/**
 * Resurrection.
 *
 * Skeletons and strays hunt in packs now! Upon being hit, a skeleton (or stray) will raise from the dead a
 * pack of bony reinforcements in a series of lightning strikes to make its attacker's life hell.
 */
public final class Resurrection extends AbstractSpecialAttack {

    private static final Set<EntityType> SUPPORTED_MOBS = new HashSet<>(Arrays.asList(
            EntityType.SKELETON, EntityType.STRAY
    ));

    public Resurrection() {
        super("resurrect", EntityDamageByEntityEvent.class, new Randomizable(), new Reinforceable());
    }

    protected boolean checkConditions(Event event) {
        LivingEntity e = Util.parseEntityDamageByEntityEvent((EntityDamageByEntityEvent) event);
        return checkDefaultConditions((EntityDamageByEntityEvent) event, e, SUPPORTED_MOBS);
    }

    @Override
    void doAttack(Event event) {
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

        LivingEntity entity = Util.parseEntityDamageByEntityEvent(e);
        if (entity == null) {
            return;
        }

        Location loc = entity.getLocation();
        Zone zone = ZoneHandler.getZone(loc);

        Randomizable randomizable = (Randomizable) getModule(ModuleType.RANDOMIZABLE);

        double probability = randomizable.getProbability(zone);
        if (Util.random(0.0, 1.0) > probability) {
            return;
        }

        World world = loc.getWorld();
        world.strikeLightningEffect(loc);
        strikeLightningAroundPoint(loc);
    }

    private void strikeLightningAroundPoint(Location loc) {
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        Reinforceable reinforceable = (Reinforceable) getModule(ModuleType.REINFORCEABLE);

        Zone zone = ZoneHandler.getZone(loc);
        int min = reinforceable.getMinReinforcements(zone);
        int max = reinforceable.getMaxReinforcements(zone);

        int numStrikes = Util.random(min, max);
        for (int i = 1; i <= numStrikes; i++) {
            Runnable lightningRunnable = () -> {
                World world = loc.getWorld();
                double xOffset = Math.pow(-1, Util.random(1, 2)) * 3 * Math.random();
                double zOffset = Math.pow(-1, Util.random(1, 2)) * 3 * Math.random();
                double xNew = x + xOffset;
                double zNew = z + zOffset;
                Location strikeLoc = new Location(world, xNew, y, zNew);
                world.strikeLightningEffect(strikeLoc);
                Runnable spawnSkeletonRunnable = () -> {
                    EntityType type = (Math.random() <= 0.2) ? EntityType.STRAY : EntityType.SKELETON;
                    type = (Math.random() <= 0.1) ? EntityType.WITHER_SKELETON : type;
                    LivingEntity livingEntity = (LivingEntity) world.spawnEntity(strikeLoc, type);
                    world.spawnParticle(Particle.ENCHANTMENT_TABLE, strikeLoc, 100);
                    Util.configureReinforcement(livingEntity);
                    livingEntity.getEquipment().setItemInMainHand(getRandomWeapon());
                };
                Bukkit.getScheduler().runTaskLater(SmartMobs.PLUGIN, spawnSkeletonRunnable, 5);
            };
            Bukkit.getScheduler().runTaskLater(SmartMobs.PLUGIN, lightningRunnable, 16*i);
        }
    }

    private static ItemStack getRandomWeapon() {
        int i = Util.random(1, 3);
        ItemStack itemStack;
        if (i == 1) {
            itemStack = new ItemStack(Material.BOW, 1);
            if (Math.random() < 0.1) {
                itemStack.addEnchantment(Enchantment.ARROW_KNOCKBACK, 3);
                itemStack.addEnchantment(Enchantment.VANISHING_CURSE, 1);
            }
        } else if (i == 2) {
            itemStack = new ItemStack(Material.STONE_SWORD, 1);
        } else {
            itemStack = new ItemStack(Material.AIR, 1);
        }
        return itemStack;
    }

}
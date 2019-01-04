package com.bermudalocket.smartmobs.attack;

import com.bermudalocket.smartmobs.Util;
import com.bermudalocket.smartmobs.Zone;
import com.bermudalocket.smartmobs.ZoneHandler;
import com.bermudalocket.smartmobs.attack.module.Randomizable;
import com.bermudalocket.smartmobs.attack.module.Reinforceable;
import com.bermudalocket.smartmobs.attack.module.ModuleType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

// ----------------------------------------------------------------------------------------------------------
/**
 * Bloat.
 *
 * An entity suddenly explodes when attacked, releasing a cloud of poison gas and sending reinforcements
 * flying in all directions.
 */
public final class Bloat extends AbstractSpecialAttack {

    // ------------------------------------------------------------------------------------------------------
    /**
     * Constructor.
     */
    public Bloat() {
        super("bloat", EntityDamageByEntityEvent.class, new Randomizable(), new Reinforceable());
    }

    // ------------------------------------------------------------------------------------------------------

    protected boolean checkConditions(Event event) {
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
        if (e.getEntity() instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) e.getEntity();
            if (livingEntity.getHealth() - e.getDamage() <= 0) {
                return false;
            } else {
                return livingEntity instanceof Zombie && !((Zombie) livingEntity).isBaby();
            }
        }
        return false;
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

        entity.setInvulnerable(true);
        entity.setAI(false);

        summonCloud(loc, 1, 3f, Color.OLIVE).setBasePotionData(new PotionData(PotionType.POISON));
        summonCloud(loc, 1, 2.5f, Color.MAROON);

        startTimedExplosion(entity);
    }

    // ------------------------------------------------------------------------
    /**
     * Starts the explosion and reinforcement summoning process.
     *
     * @param livingEntity the entity doing this attack.
     */
    private void startTimedExplosion(LivingEntity livingEntity) {
        Location loc = livingEntity.getLocation();
        doExplosion(loc);
        summonReinforcements(livingEntity);
        livingEntity.remove();
    }

    /**
     * Creates a large but harmless explosion at the given location.
     *
     * @param loc the location.
     */
    private void doExplosion(Location loc) {
        World world = loc.getWorld();
        world.spawnParticle(Particle.EXPLOSION_LARGE, loc, 1);
        world.spawnParticle(Particle.BLOCK_DUST, loc, 150, 0.2, 0.08, 0.2, 0.3, new MaterialData(Material.REDSTONE_BLOCK));
        world.spawnParticle(Particle.ITEM_CRACK, loc, 150, 0.16, 0.12, 0.16, 0.2, new ItemStack(Material.PORKCHOP));
        world.spawnParticle(Particle.BLOCK_DUST, loc, 150, new MaterialData(Material.IRON_ORE));
        world.createExplosion(loc.getX(), loc.getY(), loc.getZ(), 1f, false, true);
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1, 2);
    }

    /**
     * Summons reinforcements around the given entity.
     *
     * @param targetEntity the entity doing this attack.
     */
    private void summonReinforcements(LivingEntity targetEntity) {
        Location loc = targetEntity.getLocation();

        ItemStack mainHand = targetEntity.getEquipment().getItemInMainHand();
        if (mainHand != null && mainHand.getType() != Material.AIR) {
            World world = targetEntity.getWorld();
            Item drop = world.dropItemNaturally(loc, mainHand);
            drop.setInvulnerable(true);
        }

        Zone zone = ZoneHandler.getZone(loc);

        Reinforceable reinforceable = (Reinforceable) getModule(ModuleType.REINFORCEABLE);
        int min = reinforceable.getMinReinforcements(zone);
        int max = reinforceable.getMaxReinforcements(zone);
        int n = Util.random(min, max);

        for (int i = 1; i <= n; i++) {
            Entity entity = Util.summonEntityWithVelocity(loc, targetEntity.getType());
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                Util.configureReinforcement(livingEntity);
                if (entity instanceof Zombie) {
                    ((Zombie) entity).setBaby(true);
                }
            }
        }
    }

}
package com.bermudalocket.smartmobs.attack;

import com.bermudalocket.smartmobs.SmartMobs;
import com.bermudalocket.smartmobs.Util;
import com.bermudalocket.smartmobs.Zone;
import com.bermudalocket.smartmobs.ZoneHandler;
import com.bermudalocket.smartmobs.attack.module.Itemizable;
import com.bermudalocket.smartmobs.attack.module.ModuleType;
import com.bermudalocket.smartmobs.attack.module.Randomizable;
import com.bermudalocket.smartmobs.attack.module.Reinforceable;
import com.bermudalocket.smartmobs.util.EffectType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// ----------------------------------------------------------------------------------------------------------
/**
 * Creeper Chemistry.
 *
 * Upon explosion, a creeper launches creeper reinforcements in all directions. Those reinforcements will
 * immediately explode upon landing, releasing an {@link AreaEffectCloud}.
 */
public final class CreeperChemistry extends AbstractSpecialAttack {

    // ------------------------------------------------------------------------------------------------------
    /**
     * Constructor.
     */
    public CreeperChemistry() {
        super("CreeperChemistry",
                EntityExplodeEvent.class,
                new Randomizable(),
                new Reinforceable(),
                new Itemizable() {
                    @Override
                    public boolean verifyType(String s) {
                        return EffectType.valueOf(s) != null;
                    }
                });
    }

    // ------------------------------------------------------------------------------------------------------

    protected boolean checkConditions(Event event) {
        EntityExplodeEvent e = (EntityExplodeEvent) event;
        if (e.getEntityType() == EntityType.CREEPER) {
            return !Util.isReinforcement((Creeper) e.getEntity());
        } else {
            return false;
        }
    }

    @Override
    void doAttack(Event event) {
        EntityExplodeEvent e = (EntityExplodeEvent) event;

        Creeper creeper = (Creeper) e.getEntity();
        Location loc = creeper.getLocation();
        Zone zone = ZoneHandler.getZone(loc);

        Randomizable randomizable = (Randomizable) getModule(ModuleType.RANDOMIZABLE);
        Reinforceable reinforceable = (Reinforceable) getModule(ModuleType.REINFORCEABLE);
        Itemizable itemizable = (Itemizable) getModule(ModuleType.ITEMIZABLE);

        if (randomizable.randomize(zone)) {
            World world = loc.getWorld();
            int min = reinforceable.getMinReinforcements(zone);
            int max = reinforceable.getMaxReinforcements(zone);
            int n = Util.random(min, max);
            for (int i = 0; i < n; i++) {
                Creeper reinforceCreeper = (Creeper) Util.summonEntityWithVelocity(loc, EntityType.CREEPER);
                loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SHOOT, 1, 1);
                Runnable checkForHitGround = () -> {
                    Location reLoc = reinforceCreeper.getLocation();
                    String item = itemizable.getRandom(zone);
                    EffectType potionType;
                    try {
                        potionType = EffectType.valueOf(item);
                    } catch (Exception f) {
                        potionType = null;
                    }
                    if (potionType == null) {
                        potionType = EffectType.HARM;
                    }
                    imitateLingeringPotion(reLoc, potionType.getBukkitType());
                    doExplosion(reLoc);
                    reinforceCreeper.remove();
                    world.spawnParticle(Particle.BLOCK_DUST, reLoc, 150, 0.2, 0.08, 0.2, 0.3, new MaterialData(Material.SLIME_BLOCK));
                };
                int randomDelay = Util.random(12, 25);
                Bukkit.getScheduler().runTaskLater(SmartMobs.PLUGIN, checkForHitGround, i + randomDelay);
            }
        }
    }

    private void doExplosion(Location loc) {
        World world = loc.getWorld();
        world.spawnParticle(Particle.EXPLOSION_LARGE, loc, 1);
        world.spawnParticle(Particle.BLOCK_DUST, loc, 150, 0.2, 0.08, 0.2, 0.3, new MaterialData(Material.SLIME_BLOCK));
        world.playSound(loc, Sound.ENTITY_CREEPER_HURT, 1, 1);
    }

    private static void imitateLingeringPotion(Location loc, PotionEffectType type) {
        World world = loc.getWorld();
        AreaEffectCloud cloud = (AreaEffectCloud) world.spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD);
        cloud.addCustomEffect(new PotionEffect(type, Util.TPS*5, 1), true);
        cloud.setDuration(Util.TPS*5);

        EffectType effectType = EffectType.of(type);

        Particle particle = null;
        Color color = null;



        switch (effectType) {
            case ABSORBPTION:
                color = Color.YELLOW;
                particle = Particle.SPELL_INSTANT;
                break;
            case BLINDNESS:
                color = Color.TEAL;
                particle = Particle.SPELL;
                break;
            case CONFUSION:
                color = Color.MAROON;
                particle = Particle.SPELL_INSTANT;
                break;
            case DAMAGE_RESISTANCE:
                color = Color.RED;
                particle = Particle.SMOKE_NORMAL;
                break;
            case FAST_DIGGING:
                color = Color.SILVER;
                particle = Particle.FOOTSTEP;
                break;
            case FIRE_RESISTANCE:
                color = Color.ORANGE;
                particle = Particle.FLAME;
                break;
            case GLOWING:
                color = Color.WHITE;
                particle = Particle.SPELL_INSTANT;
                break;
            case HARM:
                color = Color.RED;
                particle = Particle.SPELL_INSTANT;
                break;
            case HEAL:
                color = Color.LIME;
                particle = Particle.VILLAGER_HAPPY;
                break;
            case HUNGER:
                color = Color.MAROON;
                particle = Particle.EXPLOSION_NORMAL;
                break;
            case INCREASE_DAMAGE:
                color = Color.ORANGE;
                particle = Particle.SPELL_INSTANT;
                break;
            case INVISIBILITY:
                color = Color.BLACK;
                particle = Particle.SPELL_WITCH;
                break;
            case JUMP:
                color = Color.GREEN;
                particle = Particle.FALLING_DUST;
                break;
            case LEVITATION:
                color = Color.TEAL;
                particle = Particle.ENCHANTMENT_TABLE;
                break;
            case NIGHT_VISION:
                color = Color.PURPLE;
                particle = Particle.PORTAL;
                break;
            case POISON:
                color = Color.RED;
                particle = Particle.VILLAGER_ANGRY;
                break;
            case REGENERATION:
                color = Color.YELLOW;
                particle = Particle.CRIT;
                break;
            case SATURATION:
                color = Color.ORANGE;
                particle = Particle.EXPLOSION_NORMAL;
                break;
            case SLOW_DIGGING:
                color = Color.GRAY;
                particle = Particle.FALLING_DUST;
                break;
            case SLOW:
                color = Color.AQUA;
                particle = Particle.CRIT;
                break;
            case SPEED:
                color = Color.ORANGE;
                particle = Particle.FALLING_DUST;
                break;
            case WATER_BREATHING:
                color = Color.AQUA;
                particle = Particle.WATER_DROP;
                break;
            case WEAKNESS:
                color = Color.GREEN;
                particle = Particle.SPELL_INSTANT;
                break;
            case WITHER:
                color = Color.BLACK;
                particle = Particle.SPELL_INSTANT;
                break;
        }
        cloud.setColor(color);
        cloud.setParticle(particle);
    }

}

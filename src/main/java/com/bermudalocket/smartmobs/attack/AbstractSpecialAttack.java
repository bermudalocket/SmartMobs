package com.bermudalocket.smartmobs.attack;

import com.bermudalocket.smartmobs.ExemptionHandler;
import com.bermudalocket.smartmobs.SmartMobs;
import com.bermudalocket.smartmobs.Util;
import com.bermudalocket.smartmobs.attack.module.AbstractModule;
import com.bermudalocket.smartmobs.attack.module.ModuleType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.potion.PotionData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// ----------------------------------------------------------------------------------------------------------
/**
 * Represents an abstract special attack.
 *
 * The idea is that an attack class is only instantiated once (when the plugin loads), but can be fired many
 * times. This prevents needless garbage collecting and reinstatiation.
 *
 * During construction, an attack is given a specific {@link Event} class to listen to. The creator can then
 * override the this#checkConditions method to fine-tune when this attack should occur.
 */
public abstract class AbstractSpecialAttack implements Listener {

    // ------------------------------------------------------------------------------------------------------
    /**
     * The name of this attack.
     */
    private final String _name;

    /**
     * The state of this attack.
     */
    private boolean _enabled;

    /**
     * The modules set for this attack.
     */
    private final Set<AbstractModule> _modules = new HashSet<>();

    // ------------------------------------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param name the name of this attack.
     * @param eventClass the {@link Event} class this attack should listen to.
     * @param modules the {@link AbstractModule}s this attack uses.
     */
    AbstractSpecialAttack(String name, Class<? extends Event> eventClass, AbstractModule... modules) {
        _name = name;
        Collections.addAll(_modules, modules);
        dynamicallyRegisterEvent(eventClass);
    }

    // ------------------------------------------------------------------------
    /**
     * Summons an {@link AreaEffectCloud} at the given location with given properties.
     *
     * @param loc the location.
     * @param duration the duration in seconds.
     * @param radius the radius in blocks.
     * @param color the color.
     * @return the created {@link AreaEffectCloud}.
     */
    static AreaEffectCloud summonCloud(Location loc, double duration, float radius, Color color) {
        return summonCloud(loc, duration, radius, color, null, null);
    }

    // ------------------------------------------------------------------------
    /**
     * Summons an {@link AreaEffectCloud} at the given location with given properties.
     *
     * @param loc the location.
     * @param duration the duration in seconds.
     * @param radius the radius in blocks.
     * @param color the color.
     * @return the created {@link AreaEffectCloud}.
     */
    static AreaEffectCloud summonCloud(Location loc, double duration, float radius, Color color, Particle particle) {
        return summonCloud(loc, duration, radius, color, particle, null);
    }

    // ------------------------------------------------------------------------
    /**
     * Summons an {@link AreaEffectCloud} at the given location with given properties.
     *
     * @param loc the location.
     * @param duration the duration in seconds.
     * @param radius the radius in blocks.
     * @param color the color.
     * @param particle the particle type.
     * @return the created {@link AreaEffectCloud}.
     */
    private static AreaEffectCloud summonCloud(Location loc, double duration, float radius, Color color, Particle particle, PotionData potionData) {
        World world = loc.getWorld();
        AreaEffectCloud cloud = (AreaEffectCloud) world.spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD);
        cloud.setColor(color);
        cloud.setRadius(radius);
        if (potionData != null) {
            cloud.setBasePotionData(potionData);
        }
        if (particle != null) {
            cloud.setParticle(particle);
        }
        cloud.setDuration((int) (Util.TPS * duration));
        return cloud;
    }

    // ------------------------------------------------------------------------
    /**
     * Sets the state of this attack.
     *
     * @param state the state.
     */
    public void setEnabled(boolean state) {
        _enabled = state;
    }

    /**
     * Dynamically registers this class as a listener for the {@link Event} class defined during construction.
     *
     * @param eventClass the {@link Event} class.
     */
    private void dynamicallyRegisterEvent(Class<? extends Event> eventClass) {
        EventExecutor eventExecutor = (listener, event) -> {
            if (eventClass.isInstance(event) && checkConditions(event) && checkEligibility(event)) {
                doAttack(event);
            }
        };
        Bukkit.getPluginManager().registerEvent(eventClass, this, EventPriority.HIGHEST, eventExecutor, SmartMobs.PLUGIN);
    }

    /**
     * Checks an event's eligibility to cause this attack to occcur.
     *
     * @param event the event.
     * @return true if the event can cause this attack to occur.
     */
    private boolean checkEligibility(Event event) {
        if (!_enabled) {
            return false;
        }
        if (event instanceof EntityEvent) {
            Entity entity = ((EntityEvent) event).getEntity();
            if (ExemptionHandler.isLocationInExemptWGRegion(entity.getLocation())) {
                return false;
            }
            if (entity instanceof LivingEntity) {
                return !Util.isReinforcement((LivingEntity) entity);
            }
        }
        return true;
    }

    /**
     * Returns this attack's module corresponding to the given {@link ModuleType}.
     *
     * @param moduleType the {@link ModuleType}.
     * @return the {@link AbstractModule}.
     */
    public AbstractModule getModule(ModuleType moduleType) {
        for (AbstractModule module : _modules) {
            if (module.getType() == moduleType) {
                return module;
            }
        }
        return null;
    }

    /**
     * Returns all modules defined for this attack.
     *
     * @return all modules defined for this attack.
     */
    public Set<AbstractModule> getModules() {
        return _modules;
    }

    /**
     * Returns this attack's name.
     *
     * @return this attack's name.
     */
    public String getName() {
        return _name;
    }

    /**
     * An abstract method to be implemented when a new {@link AbstractSpecialAttack} is created. Offers more
     * customizability than the standard {@link AbstractSpecialAttack#checkEligibility(Event)} method.
     *
     * @param event the event.
     * @return true if the event can cause this attack to occur.
     */
    abstract boolean checkConditions(Event event);

    static boolean checkDefaultConditions(EntityDamageByEntityEvent event, LivingEntity e, Set<EntityType> supportedMobs) {
        if (e != null) {
            if (e.getHealth() - event.getDamage() <= 0) {
                return false;
            }
            return supportedMobs.contains(event.getEntityType());
        } else {
            return false;
        }
    }

    /**
     * A method to implement all of the logic behind the attack, i.e. dealing damage, summoning entities, etc.
     * The Event parameter is included in order to facilitate access to relevant information, i.e. the
     * entity/entities involved.
     *
     * @param event the event which triggered the attack.
     */
    abstract void doAttack(Event event);

    // ------------------------------------------------------------------------------------------------------
    /**
     * Serializes this attack's modules.
     *
     * @param configurationSection the {@link ConfigurationSection} to save to.
     */
    public void save(ConfigurationSection configurationSection) {
        configurationSection.set("enabled", _enabled);
        for (AbstractModule module : _modules) {
            module.serialize(configurationSection);
        }
    }

    /**
     * Deserializes this attack's modules.
     *
     * @param configurationSection the {@link ConfigurationSection} to read from.
     */
    public void load(ConfigurationSection configurationSection) {
        _enabled = configurationSection.getBoolean("enabled", true);
        for (AbstractModule module : _modules) {
            module.deserialize(configurationSection);
        }
    }

}
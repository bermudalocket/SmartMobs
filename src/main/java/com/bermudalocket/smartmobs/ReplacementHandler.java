package com.bermudalocket.smartmobs;

import com.bermudalocket.smartmobs.util.Replacement;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages per-zone mob replacements.
 */
final class ReplacementHandler implements Listener {

    /**
     * A mapping from each {@link Zone} to a set of per-zone replacements.
     *
     *          f: Zone -> Replacements
     */
    private static final Map<Zone, HashSet<Replacement>> REPLACEMENTS = new HashMap<>();

    /**
     * Constructor.
     */
    ReplacementHandler() {
        Bukkit.getPluginManager().registerEvents(this, SmartMobs.PLUGIN);
    }

    /**
     * Adds a replacement.
     *
     * @param zone the zone.
     * @param from the {@link EntityType} to be replaced.
     * @param to the {@link EntityType} to replace {@param from} with.
     * @param probability the probability of this replacement occurring, from [0.0, 1.0].
     */
    static void addReplacement(Zone zone, EntityType from, EntityType to, double probability) {
        HashSet<Replacement> zoneReplacements = REPLACEMENTS.get(zone);
        if (zoneReplacements == null) {
            HashSet<Replacement> replacements = new HashSet<>();
            replacements.add(new Replacement(from, to, probability));
            REPLACEMENTS.put(zone, replacements);
        } else {
            for (Replacement replacement : zoneReplacements) {
                if (replacement.getFrom() == from) {
                    replacement.setTo(to);
                    replacement.setProbability(probability);
                    REPLACEMENTS.put(zone, zoneReplacements);
                    return;
                }
            }
            Replacement newReplacement = new Replacement(from, to, probability);
            zoneReplacements.add(newReplacement);
            REPLACEMENTS.put(zone, zoneReplacements);
        }
    }

    /**
     * If successful, returns the {@link EntityType} to replace the given type. A failure will occur if the
     * zone does not have a defined replacement for the given type, or if the probability filter fails. Will
     * return null upon failure, representing no change.
     *
     * @param zone the zone.
     * @param from the {@link EntityType} to replace.
     * @return the new {@link EntityType} upon success, or null upon failure.
     */
    private static EntityType getReplacement(Zone zone, EntityType from) {
        HashSet<Replacement> zoneReplacements = REPLACEMENTS.get(zone);
        if (zoneReplacements != null) {
            for (Replacement replacement : zoneReplacements) {
                if (replacement.getFrom() == from) {
                    if (Math.random() <= replacement.getProbability()) {
                        return replacement.getTo();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Remove all replacements for a particular zone.
     *
     * @param zone the zone.
     */
    static void removeReplacements(Zone zone) {
        REPLACEMENTS.remove(zone);
    }

    /**
     * Remove a particular replacement from a particular zone.
     *
     * @param zone the zone.
     * @param from the {@link EntityType} to be replaced.
     * @return true if the replacement was successfully removed, or false if the given entity type does not
     * have a defined replacement in the given zone.
     */
    static boolean removeReplacement(Zone zone, EntityType from) {
        HashSet<Replacement> zoneReplacements = REPLACEMENTS.get(zone);
        if (zoneReplacements != null) {
            for (Replacement replacement : zoneReplacements) {
                if (replacement.getFrom() == from) {
                    zoneReplacements.remove(replacement);
                    REPLACEMENTS.put(zone, zoneReplacements);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns all replacements for a given zone.
     *
     * @param zone the zone.
     * @return a set of all replacements for the given zone.
     */
    static HashSet<Replacement> getReplacements(Zone zone) {
        return REPLACEMENTS.get(zone);
    }

    /**
     * Handles replacements whenever a creature spawns.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    protected void onCreatureSpawn(CreatureSpawnEvent e) {
        Location loc = e.getLocation();
        Zone zone = ZoneHandler.getZone(loc);
        if (zone != null) {
            EntityType from = e.getEntityType();
            EntityType to = getReplacement(zone, from);
            if (to != null) {
                e.setCancelled(true);
                loc.getWorld().spawnEntity(loc, to);
            }

            // easter egg
            if (from == EntityType.VEX) {
                String playerName = (String) Util.getRandomElement(ADMINS);
                ItemStack playerHead = Util.getPlayerHead(playerName);
                e.getEntity().getEquipment().setHelmet(playerHead);
            }
        }
    }

    /**
     * A set containing the names of all administrators. Used for the Vex easter egg.
     */
    private static final Set<String> ADMINS = new HashSet<>(Arrays.asList(
            "pez252", "torteela", "Zomise", "cujobear",
            "bermudalocket", "Challenger2", "Deaygo", "Dumbo52", "redwall_hp", "totemo",
            "Bardidley", "Hollifer", "Mewcifer",
            "Barlimore", "ieuweh", "Silversunset", "Sir_Didymus"));

}
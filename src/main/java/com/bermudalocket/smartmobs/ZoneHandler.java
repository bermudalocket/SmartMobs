package com.bermudalocket.smartmobs;

import nu.nerd.entitymeta.EntityMeta;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

// ----------------------------------------------------------------------------------------------------------
/**
 * Manages zones.
 */
public final class ZoneHandler {

    // ------------------------------------------------------------------------------------------------------
    /**
     * A set containing all zones.
     */
    private static final Set<Zone> ZONES = new HashSet<>();

    /**
     * A binary operation which returns the smaller of two zones.
     */
    private static final BinaryOperator<Zone> MIN = (a, b) -> a.getRadius() < b.getRadius() ? a : b;

    // ------------------------------------------------------------------------------------------------------
    /**
     * Private constructor prevents reinstantiation.
     */
    private ZoneHandler() { }

    // ------------------------------------------------------------------------------------------------------
    /**
     * Adds a zone.
     *
     * @param zone the zone.
     */
    static void addZone(Zone zone) {
        ZONES.add(zone);
    }

    /**
     * Removes a zone.
     *
     * @param zone the zone.
     */
    static void removeZone(Zone zone) {
        ReplacementHandler.removeReplacements(zone);
        ZONES.remove(zone);
    }

    /**
     * Returns the smallest zone containing the given location.
     *
     * @param loc the location.
     * @return the smallest zone containing the location.
     */
    public static Zone getZone(Location loc) {
        return ZONES.stream().filter(z -> z.contains(loc))
                             .reduce(MIN)
                             .orElse(null);
    }

    /**
     * Finds and returns a zone with the given name, or null if one does not exist.
     *
     * @param name the name to search for.
     * @return the zone with the given name, or null if one does not exist.
     */
    public static Zone getZone(String name) {
        return ZONES.stream().filter(z -> z.getName().equalsIgnoreCase(name))
                             .findFirst()
                             .orElse(null);
    }

    /**
     * Finds and returns all zones which contain the given location, sorted in ascending order by radius.
     *
     * @param location the location.
     * @return a set of all zones containing the location.
     */
    static LinkedHashSet<Zone> getZones(Location location) {
        return ZONES.stream().filter(z -> z.contains(location))
                             .sorted(Comparator.comparing(Zone::getRadius).reversed())
                             .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Returns all zones.
     *
     * @return all zones.
     */
    static Set<Zone> getZones() {
        return ZONES;
    }

    /**
     * Returns true if the zone exists.
     *
     * @param name the name to check.
     * @return true if the zone exists.
     */
    static boolean zoneExists(String name) {
        return ZONES.stream().anyMatch(z -> z.getName().equalsIgnoreCase(name));
    }

    /**
     * Returns an entity's spawn zone stored in metadata.
     *
     * @param livingEntity the entity.
     * @return the entity's spawn zone.
     */
    static Zone getSpawnZone(LivingEntity livingEntity) {
        String zoneName = (String) EntityMeta.api().get(livingEntity, SmartMobs.PLUGIN, "spawn-zone");
        return ZoneHandler.getZone(zoneName);
    }

    /**
     * Replace the current zone set with a new one. From configuration (re)load.
     *
     * @param newZones the new zone set.
     */
    static void reloadZones(Set<Zone> newZones) {
        ZONES.clear();
        ZONES.addAll(newZones);
    }

}
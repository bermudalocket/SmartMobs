package com.bermudalocket.smartmobs;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

/**
 * Manages WorldGuard exemption regions.
 */
public class ExemptionHandler {

    /**
     * A set of all exempt WorldGuard region names.
     */
    static final Set<String> EXEMPT_WG_REGIONS = new HashSet<>();

    /**
     * Returns true if the given location is inside an exempt WorldGuard region.
     *
     * @param loc the location
     * @return true if the given location is inside an exempt WorldGuard region.
     */
    public static boolean isLocationInExemptWGRegion(Location loc) {
        if (SmartMobs.WORLDGUARD == null) {
            return false;
        }
        World wrappedWorld = BukkitAdapter.adapt(loc.getWorld());
        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(wrappedWorld);
        if (rm != null) {
            BlockVector3 vector3 = BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
            for (ProtectedRegion r : rm.getApplicableRegions(vector3)) {
                if (isRegionExempt(r.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds a new exemption.
     *
     * @param wgRegionName the name of the WorldGuard region to exempt.
     */
    static void addWorldGuardExemption(String wgRegionName) {
        EXEMPT_WG_REGIONS.add(wgRegionName);
    }

    /**
     * Removes an existing exemption.
     *
     * @param wgRegionName the name of the exempted WorldGuard region.
     */
    static void removeWorldGuardExemption(String wgRegionName) {
        EXEMPT_WG_REGIONS.remove(wgRegionName);
    }

    /**
     * Returns a set of all exempt WorldGuard regions.
     *
     * @return a set of all exempt WorldGuard regions.
     */
    static Set<String> getAllExemptions() {
        return EXEMPT_WG_REGIONS;
    }

    /**
     * Checks if a WorldGuard region is exempt.
     *
     * @param wgRegionName the name of the region to check.
     * @return true if the region is exempt.
     */
    private static boolean isRegionExempt(String wgRegionName) {
        return EXEMPT_WG_REGIONS.contains(wgRegionName);
    }

}

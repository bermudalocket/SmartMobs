package com.bermudalocket.smartmobs;

import com.bermudalocket.smartmobs.attack.Bloat;
import com.bermudalocket.smartmobs.attack.Chemistry;
import com.bermudalocket.smartmobs.attack.EnderDance;
import com.bermudalocket.smartmobs.attack.Flamethrower;
import com.bermudalocket.smartmobs.attack.Resurrection;
import com.bermudalocket.smartmobs.attack.SpecialAttackRegistry;
import com.bermudalocket.smartmobs.attack.Greneggs;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.slikey.effectlib.EffectLib;
import de.slikey.effectlib.EffectManager;
import nu.nerd.entitymeta.EntityMeta;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashSet;

import static com.bermudalocket.smartmobs.Util.CYAN;
import static com.bermudalocket.smartmobs.Util.GRAY;

// ----------------------------------------------------------------------------------------------------------
/**
 * The main plugin and event handling class.
 */
public final class SmartMobs extends JavaPlugin implements Listener {

    // ------------------------------------------------------------------------------------------------------
    /**
     * This plugin.
     */
    public static SmartMobs PLUGIN;

    /**
     * The EffectLib plugin, or null if EffectLib is not installed.
     */
    public static EffectManager EFFECT_MANAGER;

    /**
     * The WorldGuard plugin, or null if WorldGuard is not installed.
     */
    static WorldGuardPlugin WORLDGUARD;

    // ------------------------------------------------------------------------------------------------------
    /**
     * See {@link JavaPlugin#onEnable()}.
     */
    public void onEnable() {
        PLUGIN = this;

        SpecialAttackRegistry.register(new Bloat());
        SpecialAttackRegistry.register(new Resurrection());
        SpecialAttackRegistry.register(new Greneggs());
        SpecialAttackRegistry.register(new Chemistry());
        SpecialAttackRegistry.register(new EnderDance());
        SpecialAttackRegistry.register(new Flamethrower());

        Configuration.reload(false);

        findWorldGuard();
        findEffectLib();

        new ReplacementHandler();

        new Commands();

        new Statistics();

        Bukkit.getPluginManager().registerEvents(PLUGIN, PLUGIN);

        // remove after fundraiser
        World world = Bukkit.getWorld("world");
        HashSet<Location> locations = new HashSet<>(Arrays.asList(
                new Location(world, -1126, 0, -1043),
                new Location(world, -876, 0, 406),
                new Location(world, 337, 0, 460),
                new Location(world, 749, 0, -481),
                new Location(world, -153, 0, -566),
                new Location(world, 1329, 0, -1317),
                new Location(world, -348, 0, 562),
                new Location(world, -794, 0, -675),
                new Location(world, -516, 0, -235),
                new Location(world, -880, 0, 1157),
                new Location(world, 801, 0, 46),
                new Location(world, 985, 0, 901),
                new Location(world, -1336, 0, -41),
                new Location(world, 162, 0, 1230)));
        locations.stream().map(world::getChunkAt)
                          .forEach(world::loadChunk);
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * See {@link JavaPlugin#onDisable()}.
     */
    public void onDisable() {
        Configuration.save();
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * Determines if the {@link WorldGuardPlugin} is present. If it is, a reference to it is stored.
     */
    private void findWorldGuard() {
        Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wg instanceof WorldGuardPlugin) {
            WORLDGUARD = (WorldGuardPlugin) wg;
        } else {
            log("WorldGuard not found! Region exemptions will not work.");
        }
    }

    /**
     * Determines if the {@link EffectLib} plugin is present. If it is, a reference to it is stored.
     */
    private void findEffectLib() {
        Plugin effectLib = Bukkit.getPluginManager().getPlugin("EffectLib");
        if (effectLib instanceof EffectLib) {
            EFFECT_MANAGER = new EffectManager(effectLib);
        } else {
            log("EffectLib not found! Certain effects will not work.");
        }
    }

    // ------------------------------------------------------------------------------------------------------
    // Misc
    // ------------------------------------------------------------------------------------------------------
    /**
     * A logging convenience method, used instead of {@link java.util.logging.Logger} for colorizing this
     * plugin's name in console.
     *
     * @param message the message to log.
     */
    static void log(String message) {
        System.out.println(PREFIX + message);
        //LOGGER.info("§f[§3LOG§f] " + message);
    }

    // ------------------------------------------------------------------------------------------------------
    // Event Handlers
    // ------------------------------------------------------------------------------------------------------
    /**
     * Handles entity spawns, checking to ensure the spawn occurred in a supported World, not inside a WG
     * exemption region, and inside a defined {@link Zone}. The entity's current custom name is stored in
     * metadata via {@link EntityMeta} so that entities spawned with custom names don't lose those names.
     *
     * @param e the {@link CreatureSpawnEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    protected void onCreatureSpawn(CreatureSpawnEvent e) {
        LivingEntity livingEntity = e.getEntity();
        if (livingEntity != null) {
            World world = livingEntity.getWorld();
            if (!ExemptionHandler.isLocationInExemptWGRegion(e.getLocation())) {
                if (Configuration.isWorldInScope(world)) {

                    if (Util.PASSIVE_MOBS.contains(livingEntity.getType())) {
                        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
                                    && e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.BREEDING) {
                            e.setCancelled(true);
                            return;
                        }
                    }

                    Zone zone = ZoneHandler.getZone(e.getLocation());
                    if (zone != null) {
                        EntityMeta.api().set(livingEntity, this, "spawn-zone", zone.getName());
                        String name = livingEntity.getCustomName();
                        if (name == null) {
                            name = Util.formatEntityType(livingEntity.getType());
                        }
                        EntityMeta.api().set(livingEntity, this, "original-name", name);
                        zone.configure(livingEntity);
                    } else {
                        e.setCancelled(true);
                    }

                }
            }
        }
    }

    /**
     * Colorizes death messages in chat.
     *
     * @param e the {@link PlayerDeathEvent}.
     */
    @EventHandler
    protected void onPlayerDeath(PlayerDeathEvent e) {
        e.setDeathMessage(Configuration.DEATH_MSG_COLOR + e.getDeathMessage());
    }

    /**
     * Handles entity deaths by modifying dropped items and xp according to zone rules.
     *
     * @param e the {@link EntityDeathEvent}.
     */
    @EventHandler
    protected void onEntityDeath(EntityDeathEvent e) {
        LivingEntity livingEntity = e.getEntity();
        World world = livingEntity.getWorld();
        if (Configuration.isWorldInScope(world)) {
            Zone zone = ZoneHandler.getSpawnZone(livingEntity);
            if (zone != null) {
                int scaledXp = zone.scaleXp(e.getDroppedExp());
                e.setDroppedExp(scaledXp);
                e.getDrops().addAll(zone.getCustomDrops(e.getEntityType()));
            }
        }
    }

    /**
     * Updates a mob's health name tag whenever it takes damage.
     *
     * @param e the {@link EntityDamageEvent}.
     */
    @EventHandler
    protected void onEntityDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof LivingEntity && !ExemptionHandler.isLocationInExemptWGRegion(entity.getLocation())) {
            Bukkit.getScheduler().runTaskLater(this, () -> Util.updateMobHealthNametag((LivingEntity) entity), 1);
        }
    }

    /**
     * A pre-formatted log prefix for this plugin.
     */
    private static final String PREFIX = String.format("%s[%s%sSmartMobs%s%s] %s[%sLOG%s] %s",
            GRAY, CYAN, ChatColor.BOLD, ChatColor.RESET, GRAY,
            ChatColor.DARK_GRAY, CYAN, ChatColor.DARK_GRAY, GRAY);

}

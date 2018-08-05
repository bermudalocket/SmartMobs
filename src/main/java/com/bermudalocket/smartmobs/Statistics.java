package com.bermudalocket.smartmobs;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Statistics implements Listener {

    private static final Map<String, Integer> DEATHS_PER_ZONE = new HashMap<>();

    private static final Map<EntityType, Integer> KILLS_PER_MOB_TYPE = new HashMap<>();

    private static final Map<UUID, Integer> DEATHS_PER_PLAYER = new HashMap<>();

    private static final Map<UUID, Integer> KILLS_PER_PLAYER = new HashMap<>();

    Statistics() {
        Bukkit.getPluginManager().registerEvents(this, SmartMobs.PLUGIN);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        if (entity instanceof LivingEntity && entity.getKiller() instanceof Player) {
            Player player = entity.getKiller();
            UUID uuid = player.getUniqueId();
            int currentScore;
            if (KILLS_PER_PLAYER.containsKey(uuid)) {
                currentScore = KILLS_PER_PLAYER.get(uuid) + 1;
            } else {
                currentScore = 1;
            }
            KILLS_PER_PLAYER.put(uuid, currentScore);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (player != null) {

            // increment player deaths
            UUID uuid = player.getUniqueId();
            int currentScore;
            if (DEATHS_PER_PLAYER.containsKey(uuid)) {
                currentScore = DEATHS_PER_PLAYER.get(uuid) + 1;
            } else {
                currentScore = 1;
            }
            DEATHS_PER_PLAYER.put(uuid, currentScore);

            // increment mob type kills
            if (player.getLastDamageCause().getEntity() != null) {
                Entity entity = player.getLastDamageCause().getEntity();
                EntityType type = entity.getType();
                int currentScoreMob;
                if (KILLS_PER_MOB_TYPE.containsKey(type)) {
                    currentScoreMob = KILLS_PER_MOB_TYPE.get(type) + 1;
                } else {
                    currentScoreMob = 1;
                }
                KILLS_PER_MOB_TYPE.put(type, currentScoreMob);

                // deaths per zone
                Zone zone = ZoneHandler.getZone(player.getLocation());
                if (zone != null) {
                    int currScore;
                    String zoneName = zone.getName();
                    if (DEATHS_PER_ZONE.containsKey(zoneName)) {
                        currScore = DEATHS_PER_ZONE.get(zoneName) + 1;
                    } else {
                        currScore = 1;
                    }
                    DEATHS_PER_ZONE.put(zoneName, currScore);
                }
            }

        }
    }

    static void serialize(ConfigurationSection configurationSection) {
        for (String zoneName : DEATHS_PER_ZONE.keySet()) {
            configurationSection.set("deaths-per-zone." + zoneName, DEATHS_PER_ZONE.get(zoneName));
        }
        for (EntityType entityType : KILLS_PER_MOB_TYPE.keySet()) {
            configurationSection.set("kills-per-mob-type." + entityType.toString(), KILLS_PER_MOB_TYPE.get(entityType));
        }
        for (UUID playerUuid : DEATHS_PER_PLAYER.keySet()) {
            configurationSection.set("deaths-per-player." + playerUuid.toString(), DEATHS_PER_PLAYER.get(playerUuid));
        }
        for (UUID playerUuid : KILLS_PER_PLAYER.keySet()) {
            configurationSection.set("kills-per-player." + playerUuid.toString(), KILLS_PER_PLAYER.get(playerUuid));
        }
    }

    static void deserialize(ConfigurationSection configurationSection) {
        ConfigurationSection deathsPerZoneSection = configurationSection.getConfigurationSection("deaths-per-zone");
        if (deathsPerZoneSection != null) {
            for (String zoneName : deathsPerZoneSection.getKeys(false)) {
                DEATHS_PER_ZONE.put(zoneName, deathsPerZoneSection.getInt(zoneName));
            }
        }
        ConfigurationSection killsPerMobTypeSection = configurationSection.getConfigurationSection("kills-per-mob-type");
        if (killsPerMobTypeSection != null) {
            for (String mobTypeName : killsPerMobTypeSection.getKeys(false)) {
                try {
                    EntityType entityType = EntityType.valueOf(mobTypeName);
                    KILLS_PER_MOB_TYPE.put(entityType, killsPerMobTypeSection.getInt(mobTypeName));
                } catch (Exception ignored) {

                }
            }
        }
        ConfigurationSection deathsPerPlayerSection = configurationSection.getConfigurationSection("deaths-per-player");
        if (deathsPerPlayerSection != null) {
            for (String uuidString : deathsPerPlayerSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                DEATHS_PER_PLAYER.put(uuid, deathsPerZoneSection.getInt(uuidString));
            }
        }
        ConfigurationSection killsPerPlayerSection = configurationSection.getConfigurationSection("kills-per-player");
        if (killsPerPlayerSection != null) {
            for (String uuidString : killsPerPlayerSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                KILLS_PER_PLAYER.put(uuid, killsPerPlayerSection.getInt(uuidString));
            }
        }
    }

}

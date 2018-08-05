package com.bermudalocket.smartmobs;

import com.bermudalocket.smartmobs.attack.AbstractSpecialAttack;
import com.bermudalocket.smartmobs.attack.SpecialAttackRegistry;
import com.bermudalocket.smartmobs.util.Replacement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The configuration class responsible for interacting with the YAML configuration and serialized data.
 */
final class Configuration {

    /**
     * A set of all {@link World}s that this plugin should manage.
     */
    private static final Set<World> WORLDS = new HashSet<>();

    static ChatColor DEATH_MSG_COLOR = ChatColor.GRAY;

    /**
     * Private constructor prevents reinstantiation.
     */
    private Configuration() { }

    /**
     * Reloads the configuration.
     *
     * @param save if true the configuration will be saved before reloading.
     */
    static void reload(boolean save) {
        SmartMobs.log("Reloading configuration...");

        SmartMobs.PLUGIN.saveDefaultConfig();

        if (save) save();

        reloadTopLevel();
        reloadZones();
        reloadAttacks();
        reloadExemptions();
        reloadReplacements();
        reloadStatistics();

        SmartMobs.log("... done!");
    }

    /**
     * Serializes data to respective YAML files.
     */
    static void save() {
        try {
            serializeZones();
            serializeExemptions();
            serializeAttacks();
            serializeReplacements();
            saveStatistics();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void reloadStatistics() {
        YamlConfiguration stats = YamlConfiguration.loadConfiguration(STATISTICS_FILE);
        ConfigurationSection statSection = stats.getConfigurationSection("statistics");
        if (statSection == null) {
            statSection = stats.createSection("statistics");
        }
        Statistics.deserialize(statSection);
    }

    private static void saveStatistics() throws IOException {
        YamlConfiguration stats = YamlConfiguration.loadConfiguration(clearFile(STATISTICS_FILE));
        ConfigurationSection statSection = stats.getConfigurationSection("statistics");
        if (statSection == null) {
            statSection = stats.createSection("statistics");
        }
        Statistics.serialize(statSection);
        stats.save(STATISTICS_FILE);
    }

    /**
     * Reloads the list of managed worlds from config.yml.
     */
    private static void reloadTopLevel() {
        FileConfiguration config = SmartMobs.PLUGIN.getConfig();

        String deathMsgColorString = config.getString("death-msg-color", "GRAY");
        try {
            DEATH_MSG_COLOR = ChatColor.valueOf(deathMsgColorString);
        } catch (Exception e) {
            SmartMobs.log("Invalid chat color for death-msg-color in config.yml.");
            DEATH_MSG_COLOR = ChatColor.GRAY;
        }

        Set<World> worlds = new HashSet<>();
        for (String worldName : config.getStringList("worlds")) {
            SmartMobs.log("Looking for world " + worldName + "...");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                worlds.add(world);
                SmartMobs.log("Found world " + worldName + "!");
            } else {
                SmartMobs.log("World " + config.getString("world") + " does not exist!");
                Bukkit.getPluginManager().disablePlugin(SmartMobs.PLUGIN);
            }
        }
        WORLDS.clear();
        WORLDS.addAll(worlds);
    }

    /**
     * Serializes attacks and their customzied preferences.
     *
     * @throws IOException if the file could not be written to.
     */
    private static void serializeAttacks() throws IOException {
        YamlConfiguration serializedAttacks = YamlConfiguration.loadConfiguration(clearFile(ATTACKS_FILE));
        for (AbstractSpecialAttack attack : SpecialAttackRegistry.getAttacks()) {
            attack.save(serializedAttacks.createSection(attack.getName()));
        }
        serializedAttacks.save(ATTACKS_FILE);
    }

    /**
     * Serializes WorldGuard exemption regions as a string list.
     *
     * @throws IOException if the file could not be written to.
     */
    private static void serializeExemptions() throws IOException {
        YamlConfiguration serializedExemptions = YamlConfiguration.loadConfiguration(clearFile(EXEMPTIONS_FILE));
        List<String> exemptList = new ArrayList<>(ExemptionHandler.EXEMPT_WG_REGIONS);
        serializedExemptions.set("exemptions", exemptList);
        serializedExemptions.save(EXEMPTIONS_FILE);
    }

    /**
     * Serializes the per-zone mob replacements.
     *
     * @throws IOException if the file could not be written to.
     */
    private static void serializeReplacements() throws IOException {
        YamlConfiguration serializedReplacements = YamlConfiguration.loadConfiguration(clearFile(REPLACEMENTS_FILE));
        for (Zone zone : ZoneHandler.getZones()) {
            HashSet<Replacement> replacements = ReplacementHandler.getReplacements(zone);
            if (replacements != null) {
                for (Replacement replacement : replacements) {
                    String key = zone.getName() + "." + replacement.getFrom().toString();
                    serializedReplacements.set(key + ".to", replacement.getTo().toString());
                    serializedReplacements.set(key + ".probability", replacement.getProbability());
                }
            }
        }
        serializedReplacements.save(REPLACEMENTS_FILE);
    }

    /**
     * Reloads per-zone mob replacements from the serialized data.
     */
    private static void reloadReplacements() {
        YamlConfiguration serializedReplacements = YamlConfiguration.loadConfiguration(REPLACEMENTS_FILE);
        for (String zoneName : serializedReplacements.getKeys(false)) {
            Zone zone = ZoneHandler.getZone(zoneName);
            ConfigurationSection zoneSection = serializedReplacements.getConfigurationSection(zoneName);
            for (String fromEntityTypeAsString : zoneSection.getKeys(false)) {
                String toEntityTypeAsString = (String) zoneSection.get(fromEntityTypeAsString + ".to");
                double probability = zoneSection.getDouble(fromEntityTypeAsString + ".probability");
                EntityType fromType = EntityType.valueOf(fromEntityTypeAsString);
                EntityType toType = EntityType.valueOf(toEntityTypeAsString);
                ReplacementHandler.addReplacement(zone, fromType, toType, probability);
                SmartMobs.log("Loaded replacement: " + fromType + " -> " + toType + " (" + zoneName + ")");
            }
        }
    }

    /**
     * Reloads the WorldGuard exemption regions from the serialized data.
     */
    private static void reloadExemptions() {
        YamlConfiguration serializedZones = YamlConfiguration.loadConfiguration(EXEMPTIONS_FILE);
        ExemptionHandler.EXEMPT_WG_REGIONS.addAll(serializedZones.getStringList("exemptions"));
    }

    /**
     * Reloads customized attack preferences from the serialized data.
     */
    private static void reloadAttacks() {
        YamlConfiguration serializedAttacks = YamlConfiguration.loadConfiguration(ATTACKS_FILE);
        for (String attackName : serializedAttacks.getKeys(false)) {
            AbstractSpecialAttack attack = SpecialAttackRegistry.getAttack(attackName);
            if (attack != null) {
                attack.load(serializedAttacks.getConfigurationSection(attackName));
            }
        }
    }

    /**
     * Serializes all zones to zones.txt in {@link SmartMobs#getDataFolder()}.
     *
     * @throws IOException if the file cannot be written to.
     */
    private static void serializeZones() throws IOException {
        YamlConfiguration serializedZones = YamlConfiguration.loadConfiguration(clearFile(ZONES_FILE));
        for (Zone zone : ZoneHandler.getZones()) {
            String key = "zones." + zone.getName();
            ConfigurationSection zoneSection = serializedZones.createSection(key);
            zone.save(zoneSection);
        }
        serializedZones.save(ZONES_FILE);
    }

    /**
     * Deserializes all zones from zones.txt in {@link SmartMobs#getDataFolder()}. Clears any zones that
     * share a name with a newly loaded zone.
     */
    private static void reloadZones() {
        YamlConfiguration data = YamlConfiguration.loadConfiguration(ZONES_FILE);
        ConfigurationSection serializedZones = data.getConfigurationSection("zones");
        if (serializedZones != null) {
            Set<Zone> newZones = new HashSet<>(ZoneHandler.getZones());
            for (String key : serializedZones.getKeys(false)) {
                Zone zone = new Zone(serializedZones.getConfigurationSection(key));
                newZones.removeIf(z -> z.getName().equalsIgnoreCase(key));
                newZones.add(zone);
            }
            ZoneHandler.reloadZones(newZones);
        }
    }

    /**
     * Checks if a world should be managed by this plugin.
     *
     * @param world the world.
     * @return true if the world should be managed by this plugin.
     */
    static boolean isWorldInScope(World world) {
        return WORLDS.contains(world);
    }

    /**
     * Utility method to clear a file.
     *
     * @param file the file to clear.
     * @return the cleared file.
     */
    private static File clearFile(File file) {
        String path = file.toString();
        file.delete();
        return new File(path);
    }

    /**
     * Convenience field storing the path to this plugin's data folder.
     */
    private static final String DATA_FOLDER_PATH = SmartMobs.PLUGIN.getDataFolder().getPath();

    /**
     * Convenience reference to the attack data file.
     */
    private static final File ATTACKS_FILE = new File(DATA_FOLDER_PATH + "/attacks.yml");

    /**
     * Convenience reference to the WorldGuard exemptions data file.
     */
    private static final File EXEMPTIONS_FILE = new File(DATA_FOLDER_PATH + "/exemptions.yml");

    /**
     * Convenience reference to the per-zone mob replacement data file.
     */
    private static final File REPLACEMENTS_FILE = new File(DATA_FOLDER_PATH + "/replacements.yml");

    /**
     * Conveneience reference to the zones data file.
     */
    private static final File ZONES_FILE = new File(DATA_FOLDER_PATH + "/zones.yml");

    private static final File STATISTICS_FILE = new File(DATA_FOLDER_PATH, "/statistics.yml");

}

package com.bermudalocket.smartmobs.attack.module;

import com.bermudalocket.smartmobs.Util;
import com.bermudalocket.smartmobs.Zone;
import com.bermudalocket.smartmobs.ZoneHandler;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class Randomizable extends AbstractModule {

    private static final double DEFAULT = 0.1;

    private final Map<Zone, Double> _probabilities = new HashMap<>();

    public Randomizable() {
        super(ModuleType.RANDOMIZABLE);
    }

    public String getDescription(Zone zone) {
        return "p: " + getProbability(zone);
    }

    public boolean randomize(Zone zone) {
        return Util.random(0.0, 1.0) < getProbability(zone);
    }

    public void setProbability(Zone zone, double probability) {
        _probabilities.put(zone, probability);
    }

    public double getProbability(Zone zone) {
        return _probabilities.getOrDefault(zone, DEFAULT);
    }

    public void serialize(ConfigurationSection configurationSection) {
        for (Map.Entry<Zone, Double> entry : _probabilities.entrySet()) {
            String key = "probability." + entry.getKey().getName();
            configurationSection.set(key, entry.getValue());
        }
    }

    public void deserialize(ConfigurationSection configurationSection) {
        ConfigurationSection probSection = configurationSection.getConfigurationSection("probability");
        if (probSection != null) {
            for (String key : probSection.getKeys(false)) {
                Zone zone = ZoneHandler.getZone(key);
                if (zone != null && probSection.get(key) != null) {
                    _probabilities.put(zone, (Double) probSection.get(key));
                }
            }
        }
    }

}

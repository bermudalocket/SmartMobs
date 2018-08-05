package com.bermudalocket.smartmobs.attack.module;

import com.bermudalocket.smartmobs.Zone;
import com.bermudalocket.smartmobs.ZoneHandler;
import com.bermudalocket.smartmobs.util.ClosedInterval;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public final class Reinforceable extends AbstractModule {

    private static final int MIN = 1;

    private static final int MAX = 3;

    private final Map<Zone, ClosedInterval<Integer>> _reinforcements = new HashMap<>();

    public Reinforceable() {
        super(ModuleType.REINFORCEABLE);
    }

    public String getDescription(Zone zone) {
        ClosedInterval i = _reinforcements.get(zone);
        return (i == null) ? "r: [" + MIN + ", " + MAX + "]" : "r: [" + i.getMin() + ", " + i.getMax() + "]";
    }

    public void setMinReinforcements(Zone zone, int min) {
        setReinforcements(zone, min, getReinforcements(zone).getMax());
    }

    public void setMaxReinforcements(Zone zone, int max) {
        setReinforcements(zone, getReinforcements(zone).getMin(), max);
    }

    private void setReinforcements(Zone zone, int min, int max) {
        _reinforcements.put(zone, new ClosedInterval<>(min, max));
    }

    public int getMinReinforcements(Zone zone) {
        ClosedInterval<Integer> i = _reinforcements.get(zone);
        if (i != null) {
            return (i.getMin() == null) ? MIN : i.getMin();
        } else {
            return MIN;
        }
    }

    public int getMaxReinforcements(Zone zone) {
        ClosedInterval<Integer> i = _reinforcements.get(zone);
        if (i != null) {
            return (i.getMax() == null) ? MIN : i.getMax();
        } else {
            return MAX;
        }
    }

    public ClosedInterval<Integer> getReinforcements(Zone zone) {
        ClosedInterval<Integer> i = _reinforcements.get(zone);
        return (i != null) ? i : new ClosedInterval<>(MIN, MAX);
    }

    // ------------------------------------------------------------------------------------------------------

    public void serialize(ConfigurationSection configurationSection) {
        for (Map.Entry<Zone, ClosedInterval<Integer>> entry : _reinforcements.entrySet()) {
            String key = "reinforcements." + entry.getKey().getName();
            configurationSection.set(key + ".min", entry.getValue().getMin());
            configurationSection.set(key + ".max", entry.getValue().getMax());
        }
    }

    public void deserialize(ConfigurationSection configurationSection) {
        ConfigurationSection reinforceSection = configurationSection.getConfigurationSection("reinforcements");
        if (reinforceSection != null) {

            for (String zoneName : reinforceSection.getKeys(false)) {
                Zone zone = ZoneHandler.getZone(zoneName);
                ConfigurationSection zoneSection = reinforceSection.getConfigurationSection(zoneName);
                if (zone != null && reinforceSection.get(zoneName) != null) {
                    int min = zoneSection.getInt("min");
                    int max = zoneSection.getInt("max");
                    _reinforcements.put(zone, new ClosedInterval<>(min, max));
                }
            }

        }
    }

}
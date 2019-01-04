package com.bermudalocket.smartmobs.attack.module;

import com.bermudalocket.smartmobs.Zone;
import com.bermudalocket.smartmobs.ZoneHandler;
import com.bermudalocket.smartmobs.util.NormalDistribution;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class Gaussianizable extends AbstractModule {

    private final HashMap<Zone, NormalDistribution> _distributions = new HashMap<>();

    private static final NormalDistribution DEFAULT_DISTRIBUTION = new NormalDistribution(1, 0.25);

    public Gaussianizable() {
        super(ModuleType.GAUSSIANIZABLE);
    }

    @Override
    public String getDescription(Zone zone) {
        NormalDistribution distribution = getDistribution(zone);
        return "g: [mean = " + distribution.mean() + ", stdev = " + distribution.stdev() + "] ";
    }

    public double randomize(Zone zone) {
        NormalDistribution distribution = getDistribution(zone);
        return distribution.doTrial();
    }

    private NormalDistribution getDistribution(Zone zone) {
        return _distributions.getOrDefault(zone, DEFAULT_DISTRIBUTION);
    }

    public void setDistribution(Zone zone, double mean, double stdev) {
        _distributions.put(zone, new NormalDistribution(mean, stdev));
    }

    @Override
    public void serialize(ConfigurationSection configurationSection) {
        for (Map.Entry<Zone, NormalDistribution> entry : _distributions.entrySet()) {
            NormalDistribution distribution = entry.getValue();
            configurationSection.set("gaussian.mean." + entry.getKey().getName(), distribution.mean());
            configurationSection.set("gaussian.stdev." + entry.getKey().getName(), distribution.mean());
        }
    }

    @Override
    public void deserialize(ConfigurationSection configurationSection) {
        ConfigurationSection gaussSection = configurationSection.getConfigurationSection("gaussian");
        if (gaussSection == null) {
            return;
        }

        ConfigurationSection meanSection = configurationSection.getConfigurationSection("mean");
        if (meanSection == null) {
            return;
        }

        for (String zoneName : meanSection.getKeys(false)) {
            Zone zone = ZoneHandler.getZone(zoneName);
            if (zone != null && gaussSection.get("stdev." + zoneName) != null) {
                double mean = meanSection.getDouble(zoneName);
                double stdev = gaussSection.getDouble("stdev." + zoneName);
                _distributions.put(zone, new NormalDistribution(mean, stdev));
            }
        }
    }

}

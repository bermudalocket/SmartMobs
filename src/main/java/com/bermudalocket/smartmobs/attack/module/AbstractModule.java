package com.bermudalocket.smartmobs.attack.module;

import com.bermudalocket.smartmobs.Zone;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Essentially a tag for modules.
 */
public abstract class AbstractModule {

    private ModuleType _type;

    AbstractModule(ModuleType moduleType) {
        _type = moduleType;
    }

    public abstract String getDescription(Zone zone);

    public ModuleType getType() {
        return _type;
    }

    public abstract void serialize(ConfigurationSection configurationSection);

    public abstract void deserialize(ConfigurationSection configurationSection);

}

package com.bermudalocket.smartmobs.attack.module;

import com.bermudalocket.smartmobs.Zone;
import com.bermudalocket.smartmobs.ZoneHandler;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Itemizable extends AbstractModule {

    private final Map<Zone, Set<String>> _items = new HashMap<>();

    protected Itemizable() {
        super(ModuleType.ITEMIZABLE);
    }

    public boolean add(Zone zone, String item) {
        Set<String> currentItems = getItems(zone);
        if (currentItems.contains(item)) {
            return false;
        } else {
            currentItems.add(item);
            _items.put(zone, currentItems);
            return true;
        }
    }

    public boolean remove(Zone zone, String item) {
        Set<String> currentItems = getItems(zone);
        boolean result = currentItems.remove(item);
        _items.put(zone, currentItems);
        return result;
    }

    public String getDescription(Zone zone) {
        return "c: " + String.join(", ", getItems(zone));
    }

    private Set<String> getItems(Zone zone) {
        Set<String> currentItems = _items.get(zone);
        if (currentItems == null) {
            currentItems = new HashSet<>();
        }
        return currentItems;
    }

    public String getRandom(Zone zone) {
        int N = _items.get(zone).size();
        int r = (int) (N * Math.random());
        int n = 1;
        System.out.println("N = " + N + ", r = " + r);
        for (String item : getItems(zone)) {
            System.out.println("n = " + n + " (" + item + ")");
            if (r == n) {
                return item;
            }
            n++;
        }
        return null;
    }

    public String streamRandom(Zone zone) {
        return getItems(zone).parallelStream().peek(System.out::println).findAny().orElse(null);
    }

    public boolean verifyType(String s) {
        return true;
    }

    // ------------------------------------------------------------------------------------------------------

    public void serialize(ConfigurationSection configurationSection) {
        ConfigurationSection choicesSection = configurationSection.getConfigurationSection("choices");
        if (choicesSection == null) {
            choicesSection = configurationSection.createSection("choices");
        }
        for (Zone zone : _items.keySet()) {
            List<String> list = new ArrayList<>(getItems(zone));
            choicesSection.set(zone.getName(), list);
        }
    }

    public void deserialize(ConfigurationSection configurationSection) {
        ConfigurationSection choicesSection = configurationSection.getConfigurationSection("choices");
        if (choicesSection != null) {
            for (String zoneName : choicesSection.getKeys(false)) {
                Zone zone = ZoneHandler.getZone(zoneName);
                List<String> list = choicesSection.getStringList(zoneName);
                if (zone != null && list != null) {
                    _items.remove(zone);
                    _items.put(zone, new HashSet<>(list));
                }
            }
        }
    }

}

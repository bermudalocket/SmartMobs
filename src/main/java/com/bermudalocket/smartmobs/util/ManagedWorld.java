package com.bermudalocket.smartmobs.util;

import org.bukkit.World;

public class ManagedWorld {

    private final World _world;

    private boolean _managePassiveMobs;

    private boolean _showHealthNametags;

    public ManagedWorld(World world) {
        _world = world;
    }

    public ManagedWorld setManagePassiveMobs(boolean state) {
        _managePassiveMobs = state;
        return this;
    }

    public boolean managePassiveMobs() {
        return _managePassiveMobs;
    }

    public ManagedWorld setShowHealthNametags(boolean state) {
        _showHealthNametags = state;
        return this;
    }

    public boolean showHealthNametags() {
        return _showHealthNametags;
    }

    public World getWorld() {
        return _world;
    }

    public boolean matches(World world) {
        return _world.equals(world);
    }

}

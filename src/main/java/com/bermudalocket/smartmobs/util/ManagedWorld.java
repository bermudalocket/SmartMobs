package com.bermudalocket.smartmobs.util;

import org.bukkit.World;

public class ManagedWorld {

    private final World _world;

    private boolean _managePassiveMobs;

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

    public World getWorld() {
        return _world;
    }

    public boolean matches(World world) {
        return _world.equals(world);
    }

}

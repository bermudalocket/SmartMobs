package com.bermudalocket.smartmobs.util;

import org.bukkit.entity.EntityType;

// ----------------------------------------------------------------------------------------------------------
/**
 * An abstract per-zone mob replacement.
 */
public final class Replacement {

    // ------------------------------------------------------------------------------------------------------
    /**
     * The EntityType to be replaced.
     */
    private final EntityType _from;

    /**
     * The replacement EntityType.
     */
    private EntityType _to;

    /**
     * The probability of this replacement occurring.
     */
    private double _probability;

    // ------------------------------------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param from the {@link EntityType} to replace.
     * @param to the replacement {@link EntityType}.
     * @param probability the probability of the replacement occurring.
     */
    public Replacement(EntityType from, EntityType to, double probability) {
        _from = from;
        _to = to;
        _probability = probability;
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * Returns the {@link EntityType} to be replaced.
     *
     * @return the {@link EntityType} to be replaced.
     */
    public EntityType getFrom() {
        return _from;
    }

    /**
     * Returns the replacement {@link EntityType}.
     *
     * @return the replacement {@link EntityType}.
     */
    public EntityType getTo() {
        return _to;
    }

    /**
     * Sets the replacement {@link EntityType}.
     *
     * @param entityType the new replacement {@link EntityType}.
     */
    public void setTo(EntityType entityType) {
        _to = entityType;
    }

    /**
     * Returns the probability of this replacement occurring.
     *
     * @return the probability of this replacement occurring.
     */
    public double getProbability() {
        return _probability;
    }

    /**
     * Sets the probability of this replacement occurring.
     *
     * @param probability the new probability of this replacement occurring.
     */
    public void setProbability(double probability) {
        _probability = probability;
    }

}


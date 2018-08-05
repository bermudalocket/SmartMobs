package com.bermudalocket.smartmobs.util;

// ----------------------------------------------------------------------------------------------------------
/**
 * An immutable abstract closed interval.
 *
 * @param <T> the data type.
 */
public final class ClosedInterval<T> {

    // ------------------------------------------------------------------------------------------------------
    /**
     * The interval minimum.
     */
    private T _min;

    /**
     * The interval maximum.
     */
    private T _max;

    // ------------------------------------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param min the interval minimum.
     * @param max the interval maximum.
     */
    public ClosedInterval(T min, T max) {
        _min = min;
        _max = max;
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * Returns the interval minimum.
     *
     * @return the interval minimum.
     */
    public T getMin() {
        return _min;
    }

    /**
     * Returns the interval maximum.
     *
     * @return the interval maximum.
     */
    public T getMax() {
        return _max;
    }

}

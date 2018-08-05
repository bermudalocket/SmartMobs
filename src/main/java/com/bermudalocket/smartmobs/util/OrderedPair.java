package com.bermudalocket.smartmobs.util;

// ----------------------------------------------------------------------------------------------------------
/**
 * An immutable abstract ordered pair (x, y).
 *
 * @param <T> the data type.
 */
public class OrderedPair<T> {

    // ------------------------------------------------------------------------------------------------------
    /**
     * The first element.
     */
    private T _x;

    /**
     * The second element.
     */
    private T _y;

    // ------------------------------------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param x the first element.
     * @param y the second element.
     */
    public OrderedPair(T x, T y) {
        _x = x;
        _y = y;
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * Returns the first element.
     *
     * @return the first element.
     */
    public T getX() { return _x; }

    /**
     * Returns the second element.
     *
     * @return the second element.
     */
    public T getY() { return _y; }

}

package com.bermudalocket.smartmobs.util;

import java.util.Random;

public class NormalDistribution {

    private final double _mean;

    private final double _stdev;

    public NormalDistribution(double mean, double stdev) {
        _mean = mean;
        _stdev = stdev;
    }

    public double mean() {
        return _mean;
    }

    public double stdev() {
        return _stdev;
    }

    public double doTrial() {
        return RANDOM.nextGaussian() * _stdev + _mean;
    }

    private static final Random RANDOM = new Random();

}

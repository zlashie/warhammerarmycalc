package com.warhammer.util;

/**
 * A container to hold two different probability distributions.
 */
public class HitResult {
    private final double[] standardHits;
    private final double[] lethalHits;

    public HitResult(double[] standardHits, double[] lethalHits) {
        this.standardHits = standardHits;
        this.lethalHits = lethalHits;
    }

    public double[] getStandardHits() { return standardHits; }
    public double[] getLethalHits() { return lethalHits; }
}
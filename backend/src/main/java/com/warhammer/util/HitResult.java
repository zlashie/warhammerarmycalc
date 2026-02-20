package com.warhammer.util;

/**
 * A container to hold two different probability distributions.
 */
public class HitResult {
    private final double[] standardHits;
    private final double[] lethalHits;
    private final double[] totalVisualHits; // NEW

    public HitResult(double[] standardHits, double[] lethalHits, double[] totalVisualHits) {
        this.standardHits = standardHits;
        this.lethalHits = lethalHits;
        this.totalVisualHits = totalVisualHits;
    }

    public double[] getStandardHits() { return standardHits; }
    public double[] getLethalHits() { return lethalHits; }
    public double[] getTotalVisualHits() { return totalVisualHits; }
}
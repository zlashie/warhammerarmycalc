package com.warhammer.util;

/**
 * A data container representing the partitioned probability distributions 
 * resulting from a hit phase.
 * <p>
 * This class facilitates the transition from hits to wounds by separating 
 * dice that require a Wound Roll from those that bypass it via the 
 * 'Lethal Hits' mechanic.
 */
public class HitResult {
    private final double[] standardHits;
    private final double[] lethalHits;
    private final double[] totalVisualHits; 

    /**
     * Constructs a new HitResult with partitioned distributions.
     *
     * @param standardHits      A probability distribution of hits that proceed to 
     *                          the Wound Roll phase.
     * @param lethalHits        A probability distribution of hits that triggered 
     *                          the 'Lethal Hits' rule, bypassing the Wound Roll.
     * @param totalVisualHits   The consolidated distribution representing the sum 
     *                          of all hit results, including bonus hits from 
     *                          'Sustained Hits'.
     */
    public HitResult(double[] standardHits, double[] lethalHits, double[] totalVisualHits) {
        this.standardHits = standardHits;
        this.lethalHits = lethalHits;
        this.totalVisualHits = totalVisualHits;
    }

    /**
     * @return The distribution of hits that must roll to wound.
     */
    public double[] getStandardHits() { return standardHits; }

    /**
     * @return The distribution of hits that automatically wound the target.
     */
    public double[] getLethalHits() { return lethalHits; }

    /**
     * @return The combined distribution of all successful hits. Used primarily 
     * for UI reporting and hit-phase statistics.
     */
    public double[] getTotalVisualHits() { return totalVisualHits; }
}
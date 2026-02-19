package com.warhammer.util;

/**
 * Handles the transformation of hit distributions into wound distributions.
 */
public class WoundProcessor {

    /**
     * Calculates the wound distribution (The "Happy Path" assumes a 4+ roll).
     * @param hitDist The probability array from the hit phase.
     * @param targetWoundRoll The D6 result needed (e.g., 4 for a 4+).
     * @return A new probability distribution for wounds.
     */
    public static double[] calculateWoundDistribution(double[] hitDist, int targetWoundRoll) {
        // Probability of a single wound success (e.g., 4+ is 0.5)
        double pWound = (7.0 - targetWoundRoll) / 6.0;
        
        // The maximum possible wounds cannot exceed the maximum possible hits
        double[] woundDist = new double[hitDist.length];

        // Law of Total Probability: For every possible number of hits...
        for (int hits = 0; hits < hitDist.length; hits++) {
            double probOfHits = hitDist[hits];
            
            if (probOfHits < 0.0000001) continue;

            // ...calculate the binomial distribution of wounds for that many hits
            double[] scenarioBinomial = ProbabilityMath.calculateBinomial(hits, pWound);

            // ...and weight it by the probability of that scenario occurring
            for (int w = 0; w < scenarioBinomial.length; w++) {
                woundDist[w] += scenarioBinomial[w] * probOfHits;
            }
        }
        
        return woundDist;
    }
}
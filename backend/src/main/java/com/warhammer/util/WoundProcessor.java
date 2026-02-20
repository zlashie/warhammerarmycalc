package com.warhammer.util;

import com.warhammer.dto.CalculationRequestDTO;

/**
 * Orchestrates the wounding phase of the combat math pipeline.
 * This utility applies the Law of Total Probability to transform a distribution of
 * successful hits into a distribution of successful wounds.
 */
public class WoundProcessor {

    private static final int D6_SIDES = 6;
    private static final double PROB_PER_FACE = 1.0 / 6.0;

    /**
     * Translates a hit distribution into a final wound distribution.
     * * @param hitDist The probability array where index 'i' is the chance of 'i' hits.
     * @param targetWoundRoll The required D6 result (e.g., 4 for a 4+ roll).
     * @param req The DTO containing active modifiers.
     * @return A probability distribution array for successful wounds.
     */
    public static double[] calculateWoundDistribution(double[] hitDist, int targetWoundRoll, CalculationRequestDTO req) {
        // 1. Calculate the effective success rate for a single die (Bernoulli trial)
        double[] singleDieOutcome = new double[2]; // Index 0: Fail, Index 1: Success
        calculateSingleDieWound(singleDieOutcome, targetWoundRoll, req);
        double pWound = singleDieOutcome[1];

        double[] totalWoundDist = new double[hitDist.length];

        // 2. Map the Hit distribution to a Wound distribution using the Law of Total Probability
        for (int hits = 0; hits < hitDist.length; hits++) {
            double probOfHits = hitDist[hits];
            if (probOfHits <= 0) continue;

            // For each 'n' hits, calculate the binomial distribution of 'k' wounds
            for (int wounds = 0; wounds <= hits; wounds++) {
                double probOfKWounds = binomialProbability(hits, wounds, pWound);
                totalWoundDist[wounds] += probOfHits * probOfKWounds;
            }
        }
        return totalWoundDist;
    }

    /**
     * Simulates a single D6 roll to find the aggregate success rate.
     */
    private static void calculateSingleDieWound(double[] outcomes, int target, CalculationRequestDTO req) {
        int critValue = req.getCritWoundValue(); // Get the Anti-X value (e.g., 2, 3, 4)

        for (int face = 1; face <= D6_SIDES; face++) {
            if (DiceUtility.shouldReroll(face, target, req.getWoundRerollType(), req.isDevastatingWounds(), critValue)) {
                for (int rerollFace = 1; rerollFace <= D6_SIDES; rerollFace++) {
                    processWoundFace(rerollFace, target, outcomes, PROB_PER_FACE * PROB_PER_FACE, critValue);
                }
            } else {
                processWoundFace(face, target, outcomes, PROB_PER_FACE, critValue);
            }
        }
    }

    /**
     * Evaluates a specific D6 face against wounding criteria.
     * 40k Logic: 1s always fail, 6s always wound (unless modified by rules).
     */
    private static void processWoundFace(int face, int target, double[] outcomes, double prob, int critValue) {
        boolean isCrit = face >= critValue; 
        boolean isWound = (face >= target || isCrit || face == 6) && face != 1;

        if (isWound) {
            outcomes[1] += prob;
        } else {
            outcomes[0] += prob;
        }
    }

    /**
     * Standard Binomial Formula: P(k; n, p) = (n choose k) * p^k * (1-p)^(n-k)
     */
    private static double binomialProbability(int n, int k, double p) {
        if (p <= 0 && k > 0) return 0;
        if (p >= 1 && k < n) return 0;
        return combinations(n, k) * Math.pow(p, k) * Math.pow(1 - p, n - k);
    }

    private static long combinations(int n, int k) {
        if (k < 0 || k > n) return 0;
        if (k == 0 || k == n) return 1;
        if (k > n / 2) k = n - k;
        long res = 1;
        for (int i = 1; i <= k; i++) {
            res = res * (n - i + 1) / i;
        }
        return res;
    }
}
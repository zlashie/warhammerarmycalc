package com.warhammer.util;

import com.warhammer.dto.CalculationRequestDTO;

/**
 * Orchestrates the wounding phase of the combat math pipeline.
 * This utility applies iterative convolution to transform a distribution of
 * successful hits into a distribution of successful wounds, avoiding factorial overflow.
 */
public class WoundProcessor {

    private static final int D6_SIDES = 6;
    private static final double PROB_PER_FACE = 1.0 / 6.0;

    /**
     * Translates a hit distribution into a final wound distribution using convolution.
     *
     * @param hitDist The probability array where index 'i' is the chance of 'i' hits.
     * @param targetWoundRoll The required D6 result (e.g., 4 for a 4+ roll).
     * @param req The DTO containing active modifiers.
     * @return A probability distribution array for successful wounds.
     */
    public static double[] calculateWoundDistribution(double[] hitDist, int targetWoundRoll, CalculationRequestDTO req) {
        if (hitDist == null || hitDist.length == 0) {
            return new double[]{1.0};
        }

        // 1. Calculate the effective success rate for a single die
        double[] singleDieOutcome = new double[2]; // Index 0: Fail, Index 1: Success
        calculateSingleDieWound(singleDieOutcome, targetWoundRoll, req);

        double[] totalWoundDist = new double[hitDist.length];

        // 2. Optimization: Iterative Convolution (Mirrors DamageProcessor logic)
        // Start with a distribution representing 0 hits (100% chance of 0 wounds)
        double[] currentWoundDist = {1.0};

        for (int hits = 0; hits < hitDist.length; hits++) {
            double probOfHits = hitDist[hits];

            // If this specific hit count actually happens, add its resulting wounds to the total
            if (probOfHits > 0.0000001) {
                for (int w = 0; w < currentWoundDist.length; w++) {
                    totalWoundDist[w] += currentWoundDist[w] * probOfHits;
                }
            }

            // Convolve one additional wound die to prepare for the next 'hits' iteration
            if (hits < hitDist.length - 1) {
                currentWoundDist = ProbabilityMath.convolve(currentWoundDist, singleDieOutcome);
            }
        }
        
        return totalWoundDist;
    }

    /**
     * Simulates a single D6 roll to find the aggregate success rate.
     */
    private static void calculateSingleDieWound(double[] outcomes, int target, CalculationRequestDTO req) {
        int critValue = req.getCritWoundValue(); 

        for (int face = 1; face <= D6_SIDES; face++) {
            if (DiceUtility.shouldReroll(face, target, req.getWoundRerollType(), req.isDevastatingWounds(), critValue)) {
                for (int rerollFace = 1; rerollFace <= D6_SIDES; rerollFace++) {
                    processWoundFace(rerollFace, target, outcomes, PROB_PER_FACE * PROB_PER_FACE, critValue, req);
                }
            } else {
                processWoundFace(face, target, outcomes, PROB_PER_FACE, critValue, req);
            }
        }
    }

    /**
     * Evaluates a specific D6 face against wounding criteria.
     * 40k Logic: 1s always fail, 6s always wound (unless modified by rules).
     */
    private static void processWoundFace(int face, int target, double[] outcomes, double prob, int critValue, CalculationRequestDTO req) {
        boolean isCrit = face >= critValue; 
        int effectiveRoll = req.isPlusOneToWound() ? face + 1 : face;
        boolean isWound = (effectiveRoll >= target || isCrit || face == 6) && face != 1;

        if (isWound) {
            outcomes[1] += prob;
        } else {
            outcomes[0] += prob;
        }
    }
}
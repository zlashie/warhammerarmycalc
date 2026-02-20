package com.warhammer.util;

import com.warhammer.dto.CalculationRequestDTO;

/**
 * Utility responsible for calculating the probability distribution of hits for a unit.
 * It accounts for Ballistic Skill (BS), rerolls, and "Critical" mechanics 
 * (Lethal Hits and Sustained Hits).
 */
public class HitProcessor {

    private static final int D6_SIDES = 6;
    private static final double PROB_PER_FACE = 1.0 / 6.0;
    
    // Array size 5 supports: Index 0 (Miss), 1 (Hit), 2-4 (Sustained explosions)
    private static final int STANDARD_HITS_ARRAY_SIZE = 5;
    private static final int LETHAL_HITS_ARRAY_SIZE = 2;

    /**
     * Calculates the hit distribution for an entire unit by scaling single-attack probabilities.
     * @param request The attack profile and active modifiers.
     * @return A HitResult containing separate distributions for standard hits and lethal auto-wounds.
     */
    public static HitResult calculateUnitDistribution(CalculationRequestDTO request) {
        int totalAttacks = request.getNumberOfModels() * request.getAttacksPerModel();
        if (totalAttacks <= 0) {
            return new HitResult(new double[]{1.0}, new double[]{1.0});
        }

        double[] singleStandard = new double[STANDARD_HITS_ARRAY_SIZE];
        double[] singleLethal = new double[LETHAL_HITS_ARRAY_SIZE];

        // 1. Determine the outcome of a single D6 roll
        calculateSingleDieOutcomes(singleStandard, singleLethal, request);

        // 2. Scale the single die results to the total number of attacks using convolution
        return scaleToTotalAttacks(singleStandard, singleLethal, totalAttacks);
    }

    /**
     * Iterates through all possible D6 faces to build the probability map for one attack,
     * accounting for reroll logic if applicable.
     */
    private static void calculateSingleDieOutcomes(double[] std, double[] lethal, CalculationRequestDTO req) {
        int bs = req.getBsValue();
        for (int face = 1; face <= D6_SIDES; face++) {
            if (shouldReroll(face, bs, req)) {
                // If rerolling, calculate the average outcome of the second roll
                for (int rerollFace = 1; rerollFace <= D6_SIDES; rerollFace++) {
                    processDiceFace(rerollFace, std, lethal, req, PROB_PER_FACE * PROB_PER_FACE);
                }
            } else {
                processDiceFace(face, std, lethal, req, PROB_PER_FACE);
            }
        }
    }

    /**
     * Categorizes a single dice face into its game outcome (Miss, Hit, Lethal, or Sustained).
     */
    private static void processDiceFace(int face, double[] std, double[] lethal, CalculationRequestDTO req, double prob) {
        boolean isCrit = face >= req.getCritHitValue();
        int effectiveRoll = req.isPlusOneToHit() ? face + 1 : face;
        boolean isHit = effectiveRoll >= req.getBsValue() && face != 1;

        if (isCrit && req.isLethalHits()) {
            lethal[1] += prob;
            
            if (req.isSustainedHits()) {
                applySustainedExplosionOnly(std, req.getSustainedValue(), prob);
                lethal[0] += 0; 
            } else {
                std[0] += prob; 
            }
        } else if (isCrit && req.isSustainedHits()) {
            lethal[0] += prob;
            applySustainedWithBase(std, req.getSustainedValue(), prob);
        } else if (isHit) {
            std[1] += prob;
            lethal[0] += prob;
        } else {
            std[0] += prob;
            lethal[0] += prob;
        }
    }

    /**
     * For Lethal + Sustained: The original hit is gone (it's lethal). 
     * We only add the extra hits to the standard pool.
     */
    private static void applySustainedExplosionOnly(double[] dist, String value, double prob) {
        if ("D3".equalsIgnoreCase(value)) {
            double split = prob / 3.0;
            dist[1] += split; // +1 hit
            dist[2] += split; // +2 hits
            dist[3] += split; // +3 hits
        } else {
            int bonus = parseBonusValue(value);
            if (bonus > 0 && bonus < dist.length) dist[bonus] += prob;
            else dist[0] += prob; 
        }
    }

    /**
     * For Sustained only: 1 (the hit itself) + the bonus explosions.
     */
    private static void applySustainedWithBase(double[] dist, String value, double prob) {
        if ("D3".equalsIgnoreCase(value)) {
            double split = prob / 3.0;
            dist[2] += split; // 1 + 1
            dist[3] += split; // 1 + 2
            dist[4] += split; // 1 + 3
        } else {
            int bonus = parseBonusValue(value);
            int finalIndex = Math.min(1 + bonus, dist.length - 1);
            dist[finalIndex] += prob;
        }
    }

    /**
     * Combines single-attack distributions into a total unit distribution.
     */
    private static HitResult scaleToTotalAttacks(double[] singleStd, double[] singleLethal, int totalAttacks) {
        double[] totalStd = {1.0};
        double[] totalLethal = {1.0};

        for (int i = 0; i < totalAttacks; i++) {
            totalStd = ProbabilityMath.convolve(totalStd, singleStd);
            totalLethal = ProbabilityMath.convolve(totalLethal, singleLethal);
        }
        return new HitResult(totalStd, totalLethal);
    }

    /**
     * Determines if a dice face should be rerolled based on the user's strategy.
     * Includes "Fishing for Crits" logic where users reroll successful hits to try for 6s.
     */
    private static boolean shouldReroll(int face, int bs, CalculationRequestDTO req) {
        boolean fishing = req.isSustainedHits() || req.isLethalHits();
        return DiceUtility.shouldReroll(face, bs, req.getRerollType(), fishing, req.getCritHitValue());
    }

    private static int parseBonusValue(String value) {
        try {
            return (value != null) ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
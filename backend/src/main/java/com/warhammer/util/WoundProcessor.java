package com.warhammer.util;

import com.warhammer.dto.CalculationRequestDTO;

/**
 * Orchestrates the wounding phase of the combat math pipeline.
 * <p>
 * This utility utilizes iterative convolution to transform a probability distribution 
 * of successful hits into three distinct wound distributions: Standard, Devastating, 
 * and Total. It accounts for complex 10th Edition rules including Anti-X, 
 * Devastating Wounds, and various reroll mechanics.
 */
public class WoundProcessor {

    private static final int D6_SIDES = 6;
    private static final double PROB_PER_FACE = 1.0 / 6.0;

    /**
     * Projects a hit distribution into a multi-stream wound result.
     * <p>
     * This method executes an iterative convolution loop. For each possible number of hits, 
     * it calculates the resulting wound probabilities by siphoning dice into three pools:
     * <ul>
     * <li>Standard Wounds: Successful wounds that allow saving throws.</li>
     * <li>Devastating Wounds: Critical wounds that bypass saving throws.</li>
     * <li>Total Wounds: The consolidated success distribution.</li>
     * </ul>
     *
     * @param hitDist The probability array where index 'i' represents the probability of 'i' hits.
     * @param targetWoundRoll The required D6 result to wound (e.g., 4 for a 4+ roll).
     * @param req The DTO containing active unit modifiers and toggles.
     * @return A {@link WoundResult} containing independent distributions for standard, devastating, and total wounds.
     */
    public static WoundResult calculateWoundDistribution(double[] hitDist, int targetWoundRoll, CalculationRequestDTO req) {
        if (hitDist == null || hitDist.length == 0) {
            return new WoundResult(new double[]{1.0}, new double[]{1.0}, new double[]{1.0});
        }

        // Generate the outcome probability for a single D6 roll
        double[] singleDieOutcome = new double[3]; // [0] Fail, [1] Standard Success, [2] Devastating Success
        calculateSingleDieWound(singleDieOutcome, targetWoundRoll, req);

        int maxLen = hitDist.length;
        double[] totalStdWounds = new double[maxLen];
        double[] totalDevWounds = new double[maxLen];
        double[] totalCombinedWounds = new double[maxLen];

        double[] currentStdDist = {1.0};
        double[] currentDevDist = {1.0};
        double[] currentCombinedDist = {1.0};

        for (int hits = 0; hits < maxLen; hits++) {
            double probOfHits = hitDist[hits];

            if (probOfHits > 0.0000001) {
                // Apply the Law of Total Probability to weight the current wound distributions
                for (int i = 0; i < currentStdDist.length; i++) totalStdWounds[i] += currentStdDist[i] * probOfHits;
                for (int i = 0; i < currentDevDist.length; i++) totalDevWounds[i] += currentDevDist[i] * probOfHits;
                for (int i = 0; i < currentCombinedDist.length; i++) totalCombinedWounds[i] += currentCombinedDist[i] * probOfHits;
            }

            if (hits < maxLen - 1) {
                // Binary siphoning logic for independent pool convolution
                double[] stdBinary = {singleDieOutcome[0] + singleDieOutcome[2], singleDieOutcome[1]};
                double[] devBinary = {singleDieOutcome[0] + singleDieOutcome[1], singleDieOutcome[2]};
                double[] totalBinary = {singleDieOutcome[0], singleDieOutcome[1] + singleDieOutcome[2]};

                currentStdDist = ProbabilityMath.convolve(currentStdDist, stdBinary);
                currentDevDist = ProbabilityMath.convolve(currentDevDist, devBinary);
                currentCombinedDist = ProbabilityMath.convolve(currentCombinedDist, totalBinary);
            }
        }
        
        return new WoundResult(totalStdWounds, totalDevWounds, totalCombinedWounds);
    }

    /**
     * Aggregates the results of a single D6 roll into success and failure pools.
     * <p>
     * Accounts for reroll logic by simulating a primary roll and, if the reroll 
     * criteria are met, convolving it with a secondary D6 outcome.
     *
     * @param outcomes The array to populate with aggregate probabilities.
     * @param target The target roll required to wound.
     * @param req The request DTO containing reroll types and specific rule toggles.
     */
    private static void calculateSingleDieWound(double[] outcomes, int target, CalculationRequestDTO req) {
        int critValue = req.getCritWoundValue(); 

        for (int face = 1; face <= D6_SIDES; face++) {
            if (DiceUtility.shouldReroll(face, target, req.getWoundRerollType(), req.isDevastatingWounds(), critValue)) {
                // Simulate reroll: apply probability across all possible second-roll outcomes
                for (int rerollFace = 1; rerollFace <= D6_SIDES; rerollFace++) {
                    processWoundFace(rerollFace, target, outcomes, PROB_PER_FACE * PROB_PER_FACE, critValue, req);
                }
            } else {
                processWoundFace(face, target, outcomes, PROB_PER_FACE, critValue, req);
            }
        }
    }

    /**
     * Evaluates a single die face against the wounding criteria and siphons the probability.
     * <p>
     * Logic Priority:
     * 1. Check for Critical Wounds (Anti-X).
     * 2. If Devastating Wounds is active, siphon criticals to the bypass-save pool.
     * 3. Evaluate standard success vs target roll (including +1/-1 modifiers).
     * 4. Enforce "Natural 1 always fails" rule.
     *
     * @param face The face of the D6 being evaluated.
     * @param target The unmodified target roll.
     * @param outcomes The aggregate outcome array.
     * @param prob The weight of this specific outcome.
     * @param critValue The threshold for a critical wound (modified by Anti-X).
     * @param req The request DTO containing modifier toggles.
     */
    private static void processWoundFace(int face, int target, double[] outcomes, double prob, int critValue, CalculationRequestDTO req) {
        boolean isCrit = face >= critValue; 
        int effectiveRoll = req.isPlusOneToWound() ? face + 1 : face;
        
        // Natural 1s always fail; Natural 6s or modified rolls reaching target always succeed
        boolean isWound = (effectiveRoll >= target || isCrit || face == 6) && face != 1;

        if (isCrit && req.isDevastatingWounds()) {
            outcomes[2] += prob; // Siphon to Devastating pool
        } else if (isWound) {
            outcomes[1] += prob; // Siphon to Standard pool
        } else {
            outcomes[0] += prob; // Siphon to Fail pool
        }
    }
}
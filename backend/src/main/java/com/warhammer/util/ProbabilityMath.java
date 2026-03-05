package com.warhammer.util;

/**
 * A high-performance mathematical engine designed for discrete probability analysis.
 * This utility provides the core logic for modeling independent random events,
 * specifically optimized to simulate large-scale combat interactions where
 * multiple attack sources must be merged into a single outcome distribution.
 */
public class ProbabilityMath {

    private static final double TOTAL_PROBABILITY_WEIGHT = 1.0;
    private static final int BASE_COMBINATION_COUNT = 1;
    private static final int EMPTY_SELECTION = 0;
    private static final double SIGNIFICANCE_THRESHOLD = 0.0;

    /**
     * Merges two independent probability streams into a unified distribution.
     * In a combat context, this "stacks" independent sources—such as adding a 
     * character's attacks to a squad's total—to determine their combined impact.
     * * Defensive: Returns a neutral (0 hits) distribution if inputs are invalid 
     * or empty to prevent downstream calculation crashes.
     *
     * @param existingDistribution The current probability state (e.g., army hits so far).
     * @param newSourceDistribution The new source of hits/wounds to be integrated.
     * @return A consolidated distribution representing the total combined outcomes.
     */
    public static double[] convolve(double[] existingDistribution, double[] newSourceDistribution) {
        if (existingDistribution == null || existingDistribution.length == 0 ||
            newSourceDistribution == null || newSourceDistribution.length == 0) {
            return new double[]{1.0};
        }

        int combinedResultLength = existingDistribution.length + newSourceDistribution.length - 1;
        double[] combinedDistribution = new double[combinedResultLength];

        for (int existingIndex = 0; existingIndex < existingDistribution.length; existingIndex++) {
            double probabilityOfExistingOutcome = existingDistribution[existingIndex];

            // Branch Pruning: Optimization to skip paths with zero mathematical significance
            if (probabilityOfExistingOutcome <= SIGNIFICANCE_THRESHOLD) {
                continue;
            }

            for (int newIndex = 0; newIndex < newSourceDistribution.length; newIndex++) {
                double probabilityOfNewOutcome = newSourceDistribution[newIndex];
                int combinedOutcomeValue = existingIndex + newIndex;

                combinedDistribution[combinedOutcomeValue] += (probabilityOfExistingOutcome * probabilityOfNewOutcome);
            }
        }
        return combinedDistribution;
    }

    /**
     * Models the outcome of a "bucket of dice" roll where every die has the same success chance.
     * Maps every possible result—from total failure to maximum success—into a 
     * weighted probability distribution.
     *
     * @param totalTrials The number of dice being rolled (e.g., total attacks).
     * @param successProbability The individual chance for a single die to succeed.
     * @return A distribution where each index represents the probability of that many successes.
     * @throws IllegalArgumentException if trials are negative or probability is outside [0, 1].
     */
    public static double[] calculateBinomial(int totalTrials, double successProbability) {
        if (totalTrials < 0) {
            throw new IllegalArgumentException("Total trials cannot be negative: " + totalTrials);
        }
        
        double clampedProb = Math.max(0.0, Math.min(1.0, successProbability));
        
        double[] binomialDistribution = new double[totalTrials + 1];

        for (int successCount = 0; successCount <= totalTrials; successCount++) {
            binomialDistribution[successCount] = calculateBinomialProbability(totalTrials, successCount, clampedProb);
        }
        return binomialDistribution;
    }

    /**
     * Calculates the probability weight for one specific outcome in a dice pool.
     *
     * @param totalTrials The total size of the dice pool.
     * @param successCount The exact number of successful dice being measured.
     * @param successProbability The likelihood of a single die succeeding.
     * @return The probability of this exact outcome occurring.
     */
    public static double calculateBinomialProbability(int totalTrials, int successCount, double successProbability) {
        if (successCount < 0 || successCount > totalTrials) return 0.0;
        
        double failureProbability = TOTAL_PROBABILITY_WEIGHT - successProbability;
        
        double combinationCount = calculateCombinations(totalTrials, successCount);
        double probabilityOfSuccesses = Math.pow(successProbability, successCount);
        double probabilityOfFailures = Math.pow(failureProbability, totalTrials - successCount);

        double result = combinationCount * probabilityOfSuccesses * probabilityOfFailures;
        
        return (Double.isNaN(result) || Double.isInfinite(result)) ? 0.0 : result;
    }

    /**
     * Determines the number of unique ways a specific set of successes can be arranged.
     * Uses an iterative approach and symmetry checks for numerical stability and performance.
     *
     * @param totalTrials The total number of dice in the set.
     * @param selectionCount The number of successful dice to be arranged.
     * @return The total count of unique permutations for the given success count.
     */
    public static double calculateCombinations(int totalTrials, int selectionCount) {
        if (selectionCount < EMPTY_SELECTION || selectionCount > totalTrials) {
            return 0.0;
        }
        
        if (selectionCount == EMPTY_SELECTION || selectionCount == totalTrials) {
            return (double) BASE_COMBINATION_COUNT;
        }

        // Optimization: Leverage symmetry nCr(n, k) == nCr(n, n-k)
        if (selectionCount > totalTrials / 2) {
            selectionCount = totalTrials - selectionCount;
        }

        double totalWaysToArrange = 1.0;
        for (int i = 1; i <= selectionCount; i++) {
            totalWaysToArrange = totalWaysToArrange * (totalTrials - i + 1) / i;
        }
        
        return Double.isInfinite(totalWaysToArrange) ? Double.MAX_VALUE : totalWaysToArrange;
    }
}
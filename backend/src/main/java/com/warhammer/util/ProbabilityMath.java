package com.warhammer.util;

/**
 * High-performance mathematical engine for probability calculations.
 * Handles discrete math required to combine independent random events 
 * through convolution and binomial distributions.
 */
public class ProbabilityMath {

    private static final double TOTAL_PROBABILITY_WEIGHT = 1.0;
    private static final int BASE_COMBINATION_COUNT = 1;
    private static final int EMPTY_SELECTION = 0;
    private static final double SIGNIFICANCE_THRESHOLD = 0.0;

    /**
     * Combines two independent probability distributions using discrete convolution.
     * In Warhammer terms: this "adds" two units together to see their combined impact,
     * or adds a new attack's outcomes to an existing hit pool.
     *
     * @param existingDistribution The current probability state (e.g., army hits so far).
     * @param newSourceDistribution The distribution of the new source being added.
     * @return A merged distribution representing the sum of both independent events.
     */
    public static double[] convolve(double[] existingDistribution, double[] newSourceDistribution) {
        int combinedResultLength = existingDistribution.length + newSourceDistribution.length - 1;
        double[] combinedDistribution = new double[combinedResultLength];

        for (int existingIndex = 0; existingIndex < existingDistribution.length; existingIndex++) {
            double probabilityOfExistingOutcome = existingDistribution[existingIndex];

            // Optimization: Skip branches that have no mathematical weight
            if (probabilityOfExistingOutcome <= SIGNIFICANCE_THRESHOLD) {
                continue;
            }

            for (int newIndex = 0; newIndex < newSourceDistribution.length; newIndex++) {
                double probabilityOfNewOutcome = newSourceDistribution[newIndex];
                int combinedOutcomeValue = existingIndex + newIndex;

                // The probability of both independent outcomes occurring is the product of their probabilities
                combinedDistribution[combinedOutcomeValue] += (probabilityOfExistingOutcome * probabilityOfNewOutcome);
            }
        }
        return combinedDistribution;
    }

    /**
     * Generates a Binomial Distribution for a set of identical trials.
     * Suitable for standard attacks where each die has the same probability to hit/wound.
     *
     * @param totalTrials The number of dice being rolled (e.g., number of attacks).
     * @param successProbability The chance of a single die succeeding (e.g., 0.5 for a 4+).
     * @return An array where index 'k' represents the probability of achieving exactly 'k' successes.
     */
    public static double[] calculateBinomial(int totalTrials, double successProbability) {
        double[] binomialDistribution = new double[totalTrials + 1];

        for (int successCount = 0; successCount <= totalTrials; successCount++) {
            binomialDistribution[successCount] = calculateBinomialProbability(totalTrials, successCount, successProbability);
        }
        return binomialDistribution;
    }

    /**
     * Implements the Binomial Formula: P(k) = (n choose k) * p^k * (1-p)^(n-k)
     */
    public static double calculateBinomialProbability(int totalTrials, int successCount, double successProbability) {
        double failureProbability = TOTAL_PROBABILITY_WEIGHT - successProbability;
        
        double combinationCount = calculateCombinations(totalTrials, successCount);
        double probabilityOfSuccesses = Math.pow(successProbability, successCount);
        double probabilityOfFailures = Math.pow(failureProbability, totalTrials - successCount);

        return combinationCount * probabilityOfSuccesses * probabilityOfFailures;
    }

    /**
     * Calculates the number of ways to choose 'k' successes from 'n' trials (nCr).
     * Uses an iterative approach to maintain precision and prevent overflow with high attack counts.
     */
    public static double calculateCombinations(int totalTrials, int selectionCount) {
        if (selectionCount < EMPTY_SELECTION || selectionCount > totalTrials) {
            return 0;
        }
        if (selectionCount == EMPTY_SELECTION || selectionCount == totalTrials) {
            return BASE_COMBINATION_COUNT;
        }

        // Symmetry property optimization: nCr(n, k) == nCr(n, n-k)
        // We use the smaller selection count to minimize the number of iterations.
        if (selectionCount > totalTrials / 2) {
            selectionCount = totalTrials - selectionCount;
        }

        double totalWaysToArrange = 1.0;
        for (int i = 1; i <= selectionCount; i++) {
            totalWaysToArrange = totalWaysToArrange * (totalTrials - i + 1) / i;
        }
        return totalWaysToArrange;
    }
}
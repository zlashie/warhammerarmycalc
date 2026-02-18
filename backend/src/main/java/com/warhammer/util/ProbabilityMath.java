package com.warhammer.util;

/**
 * High-performance mathematical engine for probability calculations.
 * Handles the discrete math required to combine independent random events.
 */
public class ProbabilityMath {
    private static final double INITIAL_PROBABILITY_SUM = 1.0;
    private static final int COMBINATION_BASE_CASE = 1;
    private static final int COMBINATION_IDENTITY_CASE = 0;

    /**
     * Combines two independent probability distributions using discrete convolution.
     * In Warhammer terms: this "adds" two units together to see their combined impact.
     * @param distA Distribution of the first unit/army
     * @param distB Distribution of the new unit being added
     * @return A new array representing the merged probability distribution
     */
    public static double[] convolve(double[] distA, double[] distB) {
        // The resulting length is always (length A + length B - 1)
        double[] result = new double[distA.length + distB.length - 1];

        for (int i = 0; i < distA.length; i++) {
            // Optimization: Skip iterations with zero probability
            if (distA[i] == 0) continue; 
            
            for (int j = 0; j < distB.length; j++) {
                // The probability of getting (i + j) total hits is P(i) * P(j)
                result[i + j] += distA[i] * distB[j];
            }
        }
        return result;
    }

    /**
     * Generates a Binomial Distribution for a set of identical trials.
     * Suitable for standard attacks where each die has the same chance to hit.
     * @param n Total number of attacks (trials)
     * @param p Probability of a single hit (success)
     * @return Array where index 'k' is the probability of exactly 'k' hits.
     */
    public static double[] calculateBinomial(int n, double p) {
        double[] distribution = new double[n + 1];
        
        for (int k = 0; k <= n; k++) {
            distribution[k] = calculateBinomialProbability(n, k, p);
        }
        return distribution;
    }

    /**
     * Performs the Binomial Formula: P(k) = (n choose k) * p^k * (1-p)^(n-k)
     */
    private static double calculateBinomialProbability(int n, int k, double p) {
        return combinations(n, k) * Math.pow(p, k) * Math.pow(INITIAL_PROBABILITY_SUM - p, n - k);
    }

    /**
     * Calculates the number of ways to choose 'k' successes from 'n' trials (nCr).
     * Implements an iterative approach to prevent integer overflow with large attack counts.
     */
    private static double combinations(int n, int k) {
        if (k < COMBINATION_IDENTITY_CASE || k > n) return 0;
        if (k == COMBINATION_IDENTITY_CASE || k == n) return COMBINATION_BASE_CASE;
        
        // Symmetry property: nCr(n, k) == nCr(n, n-k). Using the smaller k speeds up the loop.
        if (k > n / 2) k = n - k;
        
        double result = 1.0;
        for (int i = 1; i <= k; i++) {
            result = result * (n - i + 1) / i;
        }
        return result;
    }
}
package com.warhammer.util;

import com.warhammer.dto.CalculationResultDTO;

/**
 * Responsible for transforming a raw probability distribution array 
 * into human-readable statistical insights.
 */
public class DistributionAnalyzer {

    private static final double P10_THRESHOLD = 0.10;
    private static final double P90_THRESHOLD = 0.90;
    private static final double P95_THRESHOLD = 0.95;
    private static final double SIGNIFICANCE_CUTOFF = 0.0001;
    private static final double ROUNDING_FACTOR = 10000.0;

    /**
     * Orchestrates the enrichment of the DTO with all calculated statistics.
     */
    public static void enrich(CalculationResultDTO dto, double[] dist) {
        if (dist == null || dist.length == 0) return;

        double mean = calculateMean(dist);
        double stdDev = calculateStandardDeviation(dist, mean);
        
        int p10 = findPercentile(dist, P10_THRESHOLD);
        int p90 = findPercentile(dist, P90_THRESHOLD);
        int p95 = findPercentile(dist, P95_THRESHOLD);
        int absoluteMax = findAbsoluteMax(dist);

        dto.setAvgValue(round(mean));
        dto.setAvgProb(round(dist[(int) Math.round(mean)] * 100));
        dto.setProbAtLeastAvg(round(calculateProbabilityAtLeast(dist, mean) * 100));
        
        dto.setRange80(formatRange(p10, p90));
        dto.setRangeTop5(formatRange(p95, absoluteMax));
        dto.setRangeStd(formatStandardRange(mean, stdDev));
    }

    /**
     * Calculates the Expected Value (Weighted Average) of the distribution.
     */
    private static double calculateMean(double[] dist) {
        double mean = 0;
        for (int i = 0; i < dist.length; i++) {
            mean += i * dist[i];
        }
        return mean;
    }

    /**
     * Calculates the Standard Deviation to measure how much the results vary from the mean.
     */
    private static double calculateStandardDeviation(double[] dist, double mean) {
        double variance = 0;
        for (int i = 0; i < dist.length; i++) {
            variance += dist[i] * Math.pow(i - mean, 2);
        }
        return Math.sqrt(variance);
    }

    /**
     * Finds the number of hits at which the cumulative probability reaches a specific threshold.
     */
    private static int findPercentile(double[] dist, double threshold) {
        double cumulative = 0;
        for (int i = 0; i < dist.length; i++) {
            cumulative += dist[i];
            if (cumulative >= threshold) return i;
        }
        return dist.length - 1;
    }

    /**
     * Sums the probabilities of all outcomes equal to or greater than the target value.
     */
    private static double calculateProbabilityAtLeast(double[] dist, double target) {
        double prob = 0;
        for (int i = 0; i < dist.length; i++) {
            if (i >= target) prob += dist[i];
        }
        return prob;
    }

    /**
     * Finds the highest number of hits that has a non-negligible probability of occurring.
     */
    private static int findAbsoluteMax(double[] dist) {
        for (int i = dist.length - 1; i >= 0; i--) {
            if (dist[i] > SIGNIFICANCE_CUTOFF) return i;
        }
        return 0;
    }

    /**
     * Helper to create a standard "Low - High" string representation for ranges.
     */
    private static String formatRange(int low, int high) {
        return low + " - " + high;
    }

    /**
     * Formats the range representing one standard deviation from the mean.
     */
    private static String formatStandardRange(double mean, double stdDev) {
        long low = Math.max(0, Math.round(mean - stdDev));
        long high = Math.round(mean + stdDev);
        return low + " - " + high;
    }

    /**
     * Rounds values to 4 decimal places for consistent API responses.
     */
    private static double round(double value) {
        return Math.round(value * ROUNDING_FACTOR) / ROUNDING_FACTOR;
    }
}
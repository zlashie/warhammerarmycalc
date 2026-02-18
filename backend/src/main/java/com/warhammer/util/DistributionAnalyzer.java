package com.warhammer.util;

import com.warhammer.dto.CalculationResultDTO;

public class DistributionAnalyzer {

    public static void enrich(CalculationResultDTO dto, double[] dist) {
        double expectedValue = 0;
        for (int i = 0; i < dist.length; i++) expectedValue += i * dist[i];

        double variance = 0;
        for (int i = 0; i < dist.length; i++) {
            variance += dist[i] * Math.pow(i - expectedValue, 2);
        }
        double stdDev = Math.sqrt(variance);

        int p10 = 0, p90 = 0, p95 = 0, absoluteMax = 0;
        double cumulative = 0, probAtLeastAvg = 0;
        boolean p10Set = false, p90Set = false, p95Set = false;

        for (int i = 0; i < dist.length; i++) {
            cumulative += dist[i];
            if (!p10Set && cumulative >= 0.10) { p10 = i; p10Set = true; }
            if (!p90Set && cumulative >= 0.90) { p90 = i; p90Set = true; }
            if (!p95Set && cumulative >= 0.95) { p95 = i; p95Set = true; }
            if (i >= expectedValue) probAtLeastAvg += dist[i];
            if (dist[i] > 0.0001) absoluteMax = i;
        }

        dto.setAvgValue(round(expectedValue));
        dto.setAvgProb(round(dist[(int) Math.round(expectedValue)] * 100));
        dto.setProbAtLeastAvg(round(probAtLeastAvg * 100));
        dto.setRange80(p10 + " - " + p90);
        dto.setRangeTop5(p95 + " - " + absoluteMax);
        dto.setRangeStd(Math.max(0, Math.round(expectedValue - stdDev)) + " - " + Math.round(expectedValue + stdDev));
    }

    private static double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
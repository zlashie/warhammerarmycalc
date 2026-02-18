package com.warhammer.service;

import com.warhammer.dto.CalculationRequestDTO;
import com.warhammer.dto.CalculationResultDTO;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalculatorService {

    public CalculationResultDTO calculateArmyHits(List<CalculationRequestDTO> requests) {
        double[] totalDist = {1.0};

        // 1. Calculate the combined distribution via convolution
        for (CalculationRequestDTO request : requests) {
            int n = request.getNumberOfModels() * request.getAttacksPerModel();
            double p = (7.0 - request.getBsValue()) / 6.0;
            
            double[] unitDist = calculateBinomial(n, p);
            totalDist = convolve(totalDist, unitDist);
        }

        // 2. Prepare the base Result DTO
        List<Double> probList = new ArrayList<>();
        for (double d : totalDist) probList.add(round(d));
        CalculationResultDTO result = new CalculationResultDTO(probList, totalDist.length - 1);

        // 3. Enrich with statistical analysis
        enrichWithStatistics(result, totalDist);

        return result;
    }

    /**
     * Calculates Mean, Mode, Standard Deviation, and 95% Confidence Interval.
     */
    private void enrichWithStatistics(CalculationResultDTO dto, double[] dist) {
        double expectedValue = 0;
        for (int i = 0; i < dist.length; i++) {
            expectedValue += i * dist[i];
        }

        // Variance for Standard Deviation
        double variance = 0;
        for (int i = 0; i < dist.length; i++) {
            variance += dist[i] * Math.pow(i - expectedValue, 2);
        }
        double stdDev = Math.sqrt(variance);

        // Cumulative analysis for Percentiles and Prob >= Avg
        int p10 = 0, p90 = 0, p95 = 0, absoluteMax = 0;
        double cumulative = 0;
        double probAtLeastAvg = 0;
        
        boolean p10Set = false, p90Set = false, p95Set = false;

        for (int i = 0; i < dist.length; i++) {
            cumulative += dist[i];
            
            // Percentile checks
            if (!p10Set && cumulative >= 0.10) { p10 = i; p10Set = true; }
            if (!p90Set && cumulative >= 0.90) { p90 = i; p90Set = true; }
            if (!p95Set && cumulative >= 0.95) { p95 = i; p95Set = true; }
            
            // Probability of getting at least the average
            if (i >= expectedValue) {
                probAtLeastAvg += dist[i];
            }
            
            if (dist[i] > 0.0001) absoluteMax = i;
        }

        dto.setAvgValue(round(expectedValue));
        dto.setAvgProb(round(dist[(int) Math.round(expectedValue)] * 100));
        dto.setProbAtLeastAvg(round(probAtLeastAvg * 100));
        
        dto.setRange80(p10 + " - " + p90);
        dto.setRangeStd(Math.max(0, Math.round(expectedValue - stdDev)) + " - " + Math.round(expectedValue + stdDev));
        dto.setRangeTop5(p95 + " - " + absoluteMax); 
    }

    private double[] calculateBinomial(int n, double p) {
        double[] dist = new double[n + 1];
        for (int k = 0; k <= n; k++) {
            dist[k] = binomialProbability(n, k, p);
        }
        return dist;
    }

    private double binomialProbability(int n, int k, double p) {
        return combinations(n, k) * Math.pow(p, k) * Math.pow(1 - p, n - k);
    }

    private double combinations(int n, int k) {
        if (k < 0 || k > n) return 0;
        if (k == 0 || k == n) return 1;
        if (k > n / 2) k = n - k;
        double res = 1;
        for (int i = 1; i <= k; i++) {
            res = res * (n - i + 1) / i;
        }
        return res;
    }

    private double[] convolve(double[] A, double[] B) {
        double[] result = new double[A.length + B.length - 1];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < B.length; j++) {
                result[i + j] += A[i] * B[j];
            }
        }
        return result;
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0; 
    }
}
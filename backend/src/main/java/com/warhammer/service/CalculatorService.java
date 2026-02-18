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

        for (CalculationRequestDTO request : requests) {
            int n = request.getNumberOfModels() * request.getAttacksPerModel();
            double p = (7.0 - request.getBsValue()) / 6.0;
            
            double[] unitDist = calculateBinomial(n, p);
            totalDist = convolve(totalDist, unitDist);
        }

        List<Double> probList = new ArrayList<>();
        for (double d : totalDist) probList.add(round(d));

        return new CalculationResultDTO(probList, totalDist.length - 1);
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
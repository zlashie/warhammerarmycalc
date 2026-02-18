package com.warhammer.service;

import com.warhammer.dto.CalculationRequestDTO;
import com.warhammer.dto.CalculationResultDTO;
import com.warhammer.util.ProbabilityMath;
import com.warhammer.util.DistributionAnalyzer;
import com.warhammer.util.HitProcessor; 
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalculatorService {

    public CalculationResultDTO calculateArmyHits(List<CalculationRequestDTO> requests) {
        double[] totalDist = {1.0};

        for (CalculationRequestDTO request : requests) {
            double[] unitDist = HitProcessor.calculateUnitDistribution(request);
            
            totalDist = ProbabilityMath.convolve(totalDist, unitDist);
        }

        CalculationResultDTO result = createBaseResult(totalDist);
        DistributionAnalyzer.enrich(result, totalDist);

        return result;
    }

    private CalculationResultDTO createBaseResult(double[] dist) {
        List<Double> probs = Arrays.stream(dist)
                .map(d -> Math.round(d * 10000.0) / 10000.0)
                .boxed()
                .collect(Collectors.toList());
        return new CalculationResultDTO(probs, dist.length - 1);
    }
}
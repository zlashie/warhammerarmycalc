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

/**
 * High-level orchestrator for army-wide probability calculations.
 * This service coordinates the calculation pipeline: 
 * Rule Processing -> Mathematical Convolution -> Statistical Analysis.
 */
@Service
public class CalculatorService {

    private static final double ROUNDING_PRECISION = 10000.0;
    private static final double[] INITIAL_PROBABILITY_STATE = {1.0};

    /**
     * Calculates the aggregate hit distribution for an entire list of units.
     * Iteratively processes each unit and merges their distributions.
     * @param requests List of unit attack profiles and modifiers.
     * @return Enriched DTO containing raw probabilities and statistical analysis.
     */
    public CalculationResultDTO calculateArmyHits(List<CalculationRequestDTO> requests) {
        // Start with a distribution representing 100% chance of 0 hits
        double[] totalArmyDistribution = INITIAL_PROBABILITY_STATE;

        for (CalculationRequestDTO request : requests) {
            // 1. Process rules for the individual unit
            double[] unitDistribution = HitProcessor.calculateUnitDistribution(request);
            
            // 2. Mathematically merge the unit into the total army distribution
            totalArmyDistribution = ProbabilityMath.convolve(totalArmyDistribution, unitDistribution);
        }

        // 3. Prepare the response container
        CalculationResultDTO result = createBaseResult(totalArmyDistribution);
        
        // 4. Perform statistical analysis (Mean, StdDev, Ranges) on the final result
        DistributionAnalyzer.enrich(result, totalArmyDistribution);

        return result;
    }

    /**
     * Initializes the result DTO and converts the raw double array into a 
     * rounded List for the API response.
     */
    private CalculationResultDTO createBaseResult(double[] distribution) {
        List<Double> roundedProbabilities = Arrays.stream(distribution)
                .map(this::round)
                .boxed()
                .collect(Collectors.toList());

        // Max hits is always the length of the distribution minus the zero-index
        int maxPossibleHits = distribution.length - 1;
        
        return new CalculationResultDTO(roundedProbabilities, maxPossibleHits);
    }

    /**
     * Standardized rounding utility for API consistency.
     */
    private double round(double value) {
        return Math.round(value * ROUNDING_PRECISION) / ROUNDING_PRECISION;
    }
}
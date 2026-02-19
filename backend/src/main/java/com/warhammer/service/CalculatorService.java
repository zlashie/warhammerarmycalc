package com.warhammer.service;

import com.warhammer.dto.CalculationRequestDTO;
import com.warhammer.dto.CalculationResultDTO;
import com.warhammer.util.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * High-level orchestrator for army-wide probability calculations.
 * This service coordinates the calculation pipeline: 
 * Rule Processing -> Hit Convolution -> Wound Transformation -> Statistical Analysis.
 */
@Service
public class CalculatorService {

    private static final double ROUNDING_PRECISION = 10000.0;
    private static final double[] INITIAL_PROBABILITY_STATE = {1.0};

    /**
     * Calculates aggregate hit and wound distributions for an entire list of units.
     * @param requests List of unit attack profiles and modifiers.
     * @return Enriched DTO containing raw probabilities and statistical analysis for both phases.
     */
    public CalculationResultDTO calculateArmyHits(List<CalculationRequestDTO> requests) {

        if (requests == null || requests.isEmpty()) {
            return createBaseResult(INITIAL_PROBABILITY_STATE);
        }

        // --- STAGE 1: HITS ---
        // Convolve all units into a single combined army hit distribution
        double[] totalHitDistribution = INITIAL_PROBABILITY_STATE;

        for (CalculationRequestDTO request : requests) {
            double[] unitHitDistribution = HitProcessor.calculateUnitDistribution(request);
            totalHitDistribution = ProbabilityMath.convolve(totalHitDistribution, unitHitDistribution);
        }

        // --- STAGE 2: WOUNDS ---
        // Transform the combined hit distribution into a wound distribution (4+ target)
        double[] totalWoundDistribution = WoundProcessor.calculateWoundDistribution(totalHitDistribution, 4);

        // --- STAGE 3: DAMAGE ---
        String damageValue = requests.get(0).getDamageValue();
        double[] totalDamageDistribution = DamageProcessor.calculateDamageDistribution(totalWoundDistribution, damageValue);
        

        // --- STAGE 4: PACKAGING & ANALYSIS ---
        // 1. Initialize DTO with Hit Probabilities
        CalculationResultDTO result = createBaseResult(totalHitDistribution);
        
        // 2. Add raw Wound Probabilities (rounded for the API)
        result.setWoundProbabilities(convertToRoundedList(totalWoundDistribution));
        result.setDamageProbabilities(convertToRoundedList(totalDamageDistribution));
        
        // 3. Perform statistical analysis for BOTH phases
        // This fills avgValue, range80 (hits) AND woundAvgValue, woundRange80 (wounds)
        DistributionAnalyzer.enrichHits(result, totalHitDistribution);
        DistributionAnalyzer.enrichWounds(result, totalWoundDistribution);
        DistributionAnalyzer.enrichDamage(result, totalDamageDistribution);

        return result;
    }

    /**
     * Initializes the result DTO and converts the raw double array into a 
     * rounded List for the API response.
     */
    private CalculationResultDTO createBaseResult(double[] distribution) {
        List<Double> roundedProbabilities = convertToRoundedList(distribution);
        int maxPossibleHits = distribution.length - 1;
        
        return new CalculationResultDTO(roundedProbabilities, maxPossibleHits);
    }

    /**
     * Helper to convert double arrays to rounded Lists for DTO consistency.
     */
    private List<Double> convertToRoundedList(double[] distribution) {
        return Arrays.stream(distribution)
                .map(this::round)
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * Standardized rounding utility for API consistency.
     */
    private double round(double value) {
        return Math.round(value * ROUNDING_PRECISION) / ROUNDING_PRECISION;
    }
}
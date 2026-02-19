package com.warhammer.service;

import com.warhammer.dto.CalculationRequestDTO;
import com.warhammer.dto.CalculationResultDTO;
import com.warhammer.util.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates the probability pipeline for Warhammer 40,000 combat math.
 * * This service implements a sequential calculation pipeline:
 * 1. Unit-level Hit/Wound generation.
 * 2. Army-level distribution convolution.
 * 3. Final damage transformation and statistical enrichment.
 */
@Service
public class CalculatorService {

    private static final double ROUNDING_PRECISION = 10000.0;
    private static final double[] INITIAL_STATE = {1.0};

    /**
     * Entry point for calculating the combat outcome of one or more units.
     * * @param requests List of unit profiles (Attacks, BS, Modifiers).
     * @return A detailed report containing probability distributions and statistics.
     */
    public CalculationResultDTO calculateArmyHits(List<CalculationRequestDTO> requests) {
        if (isRequestInvalid(requests)) {
            return createBaseResult(INITIAL_STATE);
        }

        // Accumulators for army-wide performance
        double[] armyHitDist = INITIAL_STATE;
        double[] armyWoundDist = INITIAL_STATE;

        for (CalculationRequestDTO request : requests) {
            // Process individual unit mechanics (Lethal, Sustained, etc.)
            HitResult unitHits = HitProcessor.calculateUnitDistribution(request);
            double[] unitWounds = calculateUnitWounds(unitHits);

            // Convolve this unit's results into the total army distribution
            armyHitDist = ProbabilityMath.convolve(armyHitDist, combineStandardAndLethal(unitHits));
            armyWoundDist = ProbabilityMath.convolve(armyWoundDist, unitWounds);
        }

        // Final phase: Transform wounds into damage and enrich with statistics
        return finalizeResults(requests, armyHitDist, armyWoundDist);
    }

    /**
     * Translates a unit's split hit streams into a unified wound distribution.
     * Merges 'Lethal Hits' (auto-wounds) with 'Standard Hits' that passed a wound roll.
     */
    private double[] calculateUnitWounds(HitResult hits) {
        // TODO: Replace hardcoded '4' with target-specific toughness logic
        double[] standardWounds = WoundProcessor.calculateWoundDistribution(hits.getStandardHits(), 4);
        
        // Lethal hits are already wounds; we convolve them to add them to the standard successes
        return ProbabilityMath.convolve(standardWounds, hits.getLethalHits());
    }

    /**
     * Combines standard hits and lethal hits into a single array for UI visualization.
     */
    private double[] combineStandardAndLethal(HitResult hits) {
        return ProbabilityMath.convolve(hits.getStandardHits(), hits.getLethalHits());
    }

    /**
     * Performs final damage calculation and attaches statistical analysis (Averages, Ranges, etc.)
     */
    private CalculationResultDTO finalizeResults(List<CalculationRequestDTO> requests, double[] hitDist, double[] woundDist) {
        String damageExpression = requests.get(0).getDamageValue();
        double[] damageDist = DamageProcessor.calculateDamageDistribution(woundDist, damageExpression);

        // 1. Create result with hit probabilities (This rounds hitDist)
        CalculationResultDTO result = createBaseResult(hitDist);
        
        // 2. ENRICH FIRST (High Precision)
        // These methods calculate averages/confidence intervals using the double[] arrays
        DistributionAnalyzer.enrichHits(result, hitDist);
        DistributionAnalyzer.enrichWounds(result, woundDist);
        DistributionAnalyzer.enrichDamage(result, damageDist);

        // 3. ATTACH ROUNDED LISTS LAST
        result.setWoundProbabilities(convertToRoundedList(woundDist));
        result.setDamageProbabilities(convertToRoundedList(damageDist));

        return result;
    }

    private boolean isRequestInvalid(List<CalculationRequestDTO> requests) {
        return requests == null || requests.isEmpty();
    }

    /**
     * Initializes the DTO and handles precision rounding for the API response.
     */
    private CalculationResultDTO createBaseResult(double[] distribution) {
        List<Double> roundedProbabilities = convertToRoundedList(distribution);
        return new CalculationResultDTO(roundedProbabilities, distribution.length - 1);
    }

    private List<Double> convertToRoundedList(double[] distribution) {
        return Arrays.stream(distribution)
                .map(v -> Math.round(v * ROUNDING_PRECISION) / ROUNDING_PRECISION)
                .boxed()
                .collect(Collectors.toList());
    }
}
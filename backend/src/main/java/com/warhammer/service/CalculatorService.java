package com.warhammer.service;

import com.warhammer.dto.CalculationRequestDTO;
import com.warhammer.dto.CalculationResultDTO;
import com.warhammer.util.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates the probability pipeline for Warhammer 40,000 combat math.
 * <p>
 * This service executes a multi-stage statistical analysis:
 * 1. <strong>Hit Generation:</strong> Calculates hit distributions for all units, accounting for modifiers.
 * 2. <strong>Standard Projection:</strong> Projects wounds and damage against a standard baseline (Toughness 4).
 * 3. <strong>Toughness Scaling:</strong> Iteratively calculates performance across the Toughness spectrum (T1-T12)
 * to generate trend analysis graphs.
 */
@Service
public class CalculatorService {

    private static final double ROUNDING_PRECISION = 10000.0;
    private static final double[] INITIAL_STATE = {1.0};
    private static final int MAX_TOUGHNESS_GRAPH = 12;

    /**
     * Primary entry point for combat analysis.
     * Calculates the aggregate outcome of an army list against both a standard target and a spectrum of toughness values.
     *
     * @param requests List of unit profiles containing stats (Attacks, BS, Strength) and active modifiers.
     * @return A {@link CalculationResultDTO} containing probability distributions, statistical averages, and scaling graphs.
     */
    public CalculationResultDTO calculateArmyHits(List<CalculationRequestDTO> requests) {
        if (isRequestInvalid(requests)) {
            return createBaseResult(INITIAL_STATE);
        }

        // 1. PRE-CALCULATE HITS
        // Hit distributions are independent of the target's Toughness, so we calculate them once and reuse them.
        List<HitResult> allUnitHits = new ArrayList<>();
        double[] armyHitDist = INITIAL_STATE;

        for (CalculationRequestDTO request : requests) {
            HitResult unitHits = HitProcessor.calculateUnitDistribution(request);
            allUnitHits.add(unitHits);
            
            // Convolve this unit's hits into the total army hit distribution
            armyHitDist = ProbabilityMath.convolve(armyHitDist, unitHits.getTotalVisualHits());
        }

        // 2. STANDARD PIPELINE (Baseline Analysis)
        // Calculates the "Wounds" and "Damage" cards assuming a standard target (Hardcoded T4).
        double[] standardArmyWounds = INITIAL_STATE;
        for (int i = 0; i < requests.size(); i++) {
            double[] unitWounds = calculateUnitWounds(allUnitHits.get(i), 4, requests.get(i));
            standardArmyWounds = ProbabilityMath.convolve(standardArmyWounds, unitWounds);
        }
        
        CalculationResultDTO result = finalizeResults(requests, armyHitDist, standardArmyWounds);

        // 3. TOUGHNESS SCALING PIPELINE (Trend Analysis)
        // Iterates from T1 to T12, recalculating wound probabilities based on S vs T breakpoints.
        List<CalculationResultDTO.ToughnessNode> scalingData = new ArrayList<>();

        for (int t = 1; t <= MAX_TOUGHNESS_GRAPH; t++) {
            double[] iterationArmyWounds = INITIAL_STATE;

            for (int i = 0; i < requests.size(); i++) {
                int requiredRoll = getWoundRoll(requests.get(i).getStrength(), t);
                
                double[] unitWounds = calculateUnitWounds(allUnitHits.get(i), requiredRoll, requests.get(i));
                iterationArmyWounds = ProbabilityMath.convolve(iterationArmyWounds, unitWounds);
            }

            scalingData.add(extractNodeStats(t, iterationArmyWounds));
        }

        result.setToughnessScaling(scalingData);

        // 4. SAVE SCALING PIPELINE (Trend Analysis)
        List<CalculationResultDTO.SaveNode> saveScalingData = new ArrayList<>();

        for (int s = 2; s <= 7; s++) {
            double[] iterationArmyDamage = INITIAL_STATE;

            for (CalculationRequestDTO request : requests) {
                HitResult unitHits = HitProcessor.calculateUnitDistribution(request);
                double[] unitWounds = calculateUnitWounds(unitHits, 4, request);

                double failProb = calculateFailProbability(s, request.getAp());
                double[] unsavedWounds = applySave(unitWounds, failProb);

                double[] unitDamage = DamageProcessor.calculateDamageDistribution(unsavedWounds, request.getDamageValue());
                iterationArmyDamage = ProbabilityMath.convolve(iterationArmyDamage, unitDamage);
            }

            String label = s > 6 ? "None" : s + "+";
            saveScalingData.add(extractSaveNodeStats(label, iterationArmyDamage));
        }

        result.setSaveScaling(saveScalingData);
        
        return result;
    }

    /**
     * Determines the required D6 result to wound a target based on 10th Edition rules.
     * <ul>
     * <li>S >= 2xT : 2+</li>
     * <li>S > T    : 3+</li>
     * <li>S = T    : 4+</li>
     * <li>S < T    : 5+</li>
     * <li>S <= T/2 : 6+</li>
     * </ul>
     */
    private int getWoundRoll(int strength, int toughness) {
        if (strength >= toughness * 2) return 2;
        if (strength > toughness)      return 3;
        if (strength == toughness)     return 4;
        if (strength <= toughness / 2) return 6;
        return 5;
    }

    /**
     * Implements 10th Ed Save Logic:
     * 1 is always a fail. AP modifies the save requirement by increasing the target number.
     */
    private double calculateFailProbability(int baseSave, int ap) {
        if (baseSave > 6) return 1.0; 
        
        int modifiedSave = baseSave + Math.abs(ap); 

        if (modifiedSave > 6) return 1.0; 
        if (modifiedSave < 2) modifiedSave = 2;
        
        return (double)(modifiedSave - 1) / 6.0;
    }

    /**
     * Convolves wound distribution with save failure chance to get unsaved wounds.
     */
    private double[] applySave(double[] woundDist, double failProb) {
        double[] singleWoundOutcome = {1.0 - failProb, failProb}; 
        
        double[] totalUnsavedDist = new double[woundDist.length];
        double[] currentUnsavedDist = {1.0};

        for (int w = 0; w < woundDist.length; w++) {
            double prob = woundDist[w];
            if (prob > 0.0000001) {
                for (int i = 0; i < currentUnsavedDist.length; i++) {
                    totalUnsavedDist[i] += currentUnsavedDist[i] * prob;
                }
            }
            if (w < woundDist.length - 1) {
                currentUnsavedDist = ProbabilityMath.convolve(currentUnsavedDist, singleWoundOutcome);
            }
        }
        return totalUnsavedDist;
    }

    /**
     * Transforms a unit's hit distribution into a wound distribution.
     * Merges "Lethal Hits" (which bypass the wound roll) with standard hits that successfully roll to wound.
     */
    private double[] calculateUnitWounds(HitResult hits, int targetWoundRoll, CalculationRequestDTO request) {
        double[] standardWounds = WoundProcessor.calculateWoundDistribution(
            hits.getStandardHits(), 
            targetWoundRoll, 
            request
        );
        
        return ProbabilityMath.convolve(standardWounds, hits.getLethalHits());
    }
    
    /**
     * Extracts key statistical markers from a probability distribution for the scaling graph.
     * Calculates the weighted Average, the 10th Percentile (Lower 80% bound), and the 90th Percentile (Upper 80% bound).
     *
     * @param toughness/save The toughness value associated with this distribution.
     * @param dist The probability distribution array.
     * @return A node containing the x-axis value (T) and y-axis stats (Avg, Low, High).
     */
    private CalculationResultDTO.ToughnessNode extractNodeStats(int toughness, double[] dist) {
        double[] stats = calculateStats(dist);
        return new CalculationResultDTO.ToughnessNode(toughness, stats[0], stats[1], stats[2]);
    }

    private CalculationResultDTO.SaveNode extractSaveNodeStats(String label, double[] dist) {
        double[] stats = calculateStats(dist);
        return new CalculationResultDTO.SaveNode(label, stats[0], stats[1], stats[2]);
    }

    private double[] calculateStats(double[] dist) {
        double avg = 0.0;
        double sumProb = 0.0;
        double lower80 = -1.0;
        double upper80 = -1.0;

        for (int i = 0; i < dist.length; i++) {
            double p = dist[i];
            if (p <= 0.0000001) continue;

            avg += i * p;
            sumProb += p;

            if (lower80 < 0 && sumProb >= 0.10) lower80 = i;
            if (upper80 < 0 && sumProb >= 0.90) upper80 = i;
        }
        
        if (lower80 < 0) lower80 = 0;
        if (upper80 < 0) upper80 = dist.length - 1;

        return new double[]{avg, lower80, upper80};
    }

    /**
     * Finalizes the DTO by calculating damage distributions and enriching the result with statistical metadata.
     * This method separates the high-precision arrays used for calculation from the rounded lists sent to the frontend.
     */
    private CalculationResultDTO finalizeResults(List<CalculationRequestDTO> requests, double[] hitDist, double[] woundDist) {
        String damageExpression = requests.get(0).getDamageValue();
        double[] damageDist = DamageProcessor.calculateDamageDistribution(woundDist, damageExpression);

        // Initialize result with Hit Probabilities
        CalculationResultDTO result = createBaseResult(hitDist);
        
        // Calculate and attach statistical metadata (Averages, Confidence Intervals, etc.)
        DistributionAnalyzer.enrichHits(result, hitDist);
        DistributionAnalyzer.enrichWounds(result, woundDist);
        DistributionAnalyzer.enrichDamage(result, damageDist);

        // Attach rounded probability lists for frontend charts
        result.setWoundProbabilities(convertToRoundedList(woundDist));
        result.setDamageProbabilities(convertToRoundedList(damageDist));

        return result;
    }

    private boolean isRequestInvalid(List<CalculationRequestDTO> requests) {
        return requests == null || requests.isEmpty();
    }

    /**
     * creates an initial Result DTO containing the hit distribution.
     */
    private CalculationResultDTO createBaseResult(double[] distribution) {
        List<Double> roundedProbabilities = convertToRoundedList(distribution);
        return new CalculationResultDTO(roundedProbabilities, distribution.length - 1);
    }

    /**
     * Converts a raw double array into a List of Doubles rounded to 4 decimal places.
     * This reduces JSON payload size and handles floating-point artifacts.
     */
    private List<Double> convertToRoundedList(double[] distribution) {
        return Arrays.stream(distribution)
                .map(v -> Math.round(v * ROUNDING_PRECISION) / ROUNDING_PRECISION)
                .boxed()
                .collect(Collectors.toList());
    }
}
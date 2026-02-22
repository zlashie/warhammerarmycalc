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
 * Orchestrates the multi-stage probability pipeline for Warhammer 40,000 combat analysis.
 * <p>
 * This service executes a series of statistical convolutions to project army-wide performance:
 * <ol>
 * <li><b>Hit Generation:</b> Aggregates distributions across all units, accounting for BS and hit modifiers.</li>
 * <li><b>Baseline Analysis:</b> Projects wounds and damage against a standard baseline target (T4).</li>
 * <li><b>Toughness Scaling:</b> Iteratively calculates performance across the Toughness spectrum (T1-T12).</li>
 * <li><b>Save Scaling:</b> Recalculates damage distributions across target armor profiles (2+ to 6+).</li>
 * </ol>
 */
@Service
public class CalculatorService {

    private static final double ROUNDING_PRECISION = 10000.0;
    private static final double[] INITIAL_STATE = {1.0};
    private static final int MAX_TOUGHNESS_GRAPH = 12;

    /**
     * Primary entry point for calculating the aggregate outcome of an army list.
     * * @param requests A list of unit profiles containing stats and active rules.
     * @return A {@link CalculationResultDTO} containing statistical averages, range data, and trend nodes.
     */
    public CalculationResultDTO calculateArmyHits(List<CalculationRequestDTO> requests) {
        if (isRequestInvalid(requests)) {
            return createBaseResult(INITIAL_STATE);
        }

        // 1. PRE-CALCULATE HITS
        // Hit distributions are independent of the target and are calculated once for the entire method scope.
        List<HitResult> allUnitHits = new ArrayList<>();
        double[] armyHitDist = INITIAL_STATE;

        for (CalculationRequestDTO request : requests) {
            HitResult unitHits = HitProcessor.calculateUnitDistribution(request);
            allUnitHits.add(unitHits);
            armyHitDist = ProbabilityMath.convolve(armyHitDist, unitHits.getTotalVisualHits());
        }

        // 2. STANDARD PIPELINE (Baseline T4 Analysis)
        // Calculates standard outcomes for the main statistical cards.
        double[] standardArmyWounds = INITIAL_STATE;
        for (int i = 0; i < requests.size(); i++) {
            WoundResult unitWoundResult = calculateUnitWounds(allUnitHits.get(i), 4, requests.get(i));
            standardArmyWounds = ProbabilityMath.convolve(standardArmyWounds, unitWoundResult.totalWounds());
        }
        
        CalculationResultDTO resultDTO = finalizeResults(requests, armyHitDist, standardArmyWounds);

        // 3. TOUGHNESS SCALING PIPELINE (Trend Analysis: T1 - T12)
        // Generates the Toughness Analysis graph data.
        List<CalculationResultDTO.ToughnessNode> toughnessScalingData = new ArrayList<>();
        for (int t = 1; t <= MAX_TOUGHNESS_GRAPH; t++) {
            double[] iterationArmyWounds = INITIAL_STATE;

            for (int i = 0; i < requests.size(); i++) {
                int requiredRoll = getWoundRoll(requests.get(i).getStrength(), t);
                WoundResult unitWoundResult = calculateUnitWounds(allUnitHits.get(i), requiredRoll, requests.get(i));
                iterationArmyWounds = ProbabilityMath.convolve(iterationArmyWounds, unitWoundResult.totalWounds());
            }
            toughnessScalingData.add(extractNodeStats(t, iterationArmyWounds));
        }
        resultDTO.setToughnessScaling(toughnessScalingData);

        // 4. SAVE SCALING PIPELINE (Trend Analysis: 2+ to None)
        // Generates the Damage Analysis graph data, accounting for bypassing rules like Devastating Wounds.
        List<CalculationResultDTO.SaveNode> saveScalingData = new ArrayList<>();
        for (int s = 2; s <= 7; s++) {
            double[] iterationArmyDamage = INITIAL_STATE;

            for (int i = 0; i < requests.size(); i++) {
                CalculationRequestDTO request = requests.get(i);
                WoundResult unitWounds = calculateUnitWounds(allUnitHits.get(i), 4, request);
                double[] unitDamage = calculateUnitDamage(unitWounds, s, request);
                
                iterationArmyDamage = ProbabilityMath.convolve(iterationArmyDamage, unitDamage);
            }

            String label = s > 6 ? "None" : s + "+";
            saveScalingData.add(extractSaveNodeStats(label, iterationArmyDamage));
        }
        resultDTO.setSaveScaling(saveScalingData);
        
        return resultDTO;
    }

    /**
     * Transforms a hit distribution into a tri-pool WoundResult.
     * <p>
     * Siphons 'Lethal Hits' into the standard pool as they are subject to saves, 
     * while 'Devastating Wounds' are kept separate to bypass the save phase.
     */
    private WoundResult calculateUnitWounds(HitResult hits, int targetWoundRoll, CalculationRequestDTO request) {
        WoundResult result = WoundProcessor.calculateWoundDistribution(
            hits.getStandardHits(), 
            targetWoundRoll, 
            request
        );

        // Lethal Hits bypass the wound roll but still allow saving throws.
        double[] standardPool = ProbabilityMath.convolve(result.standardWounds(), hits.getLethalHits());
        
        // Total pool for reporting combines standard success and bypassing success.
        double[] totalPool = ProbabilityMath.convolve(standardPool, result.devastatingWounds());
        
        return new WoundResult(standardPool, result.devastatingWounds(), totalPool);
    }

    /**
     * Projects final damage by branching wounds based on armor mitigation.
     * * @param wounds The partitioned distributions from the wound phase.
     * @param save The target's base saving throw.
     * @param req The request containing AP and damage characteristics.
     * @return The combined damage distribution for the unit.
     */
    private double[] calculateUnitDamage(WoundResult wounds, int save, CalculationRequestDTO req) {
        double failProb = calculateFailProbability(save, req.getAp());
        
        // Standard wounds (and Lethals) must pass the save check.
        double[] unsavedStandard = applySave(wounds.standardWounds(), failProb);
        
        // Devastating wounds bypass the save check and convolve directly into final unsaved wounds.
        double[] totalUnsaved = ProbabilityMath.convolve(unsavedStandard, wounds.devastatingWounds());
        
        return DamageProcessor.calculateDamageDistribution(totalUnsaved, req.getDamageValue());
    }

    /**
     * Determines the required D6 result to wound a target based on the S vs T relationship.
     */
    private int getWoundRoll(int strength, int toughness) {
        if (strength >= toughness * 2) return 2;
        if (strength > toughness) return 3;
        if (strength == toughness) return 4;
        if (strength <= toughness / 2) return 6;
        return 5;
    }

    /**
     * Calculates the probability of a save failing after AP modifications.
     */
    private double calculateFailProbability(int baseSave, int ap) {
        if (baseSave > 6) return 1.0; 
        int modifiedSave = baseSave + Math.abs(ap); 
        if (modifiedSave > 6) return 1.0; 
        if (modifiedSave < 2) modifiedSave = 2; // Natural 1 always fails
        return (double)(modifiedSave - 1) / 6.0;
    }

    /**
     * Applies a binomial success/fail check to a distribution (Saving Throw simulation).
     */
    private double[] applySave(double[] woundDist, double failProb) {
        double[] singleWoundOutcome = {1.0 - failProb, failProb}; 
        double[] totalUnsavedDist = new double[woundDist.length];
        double[] currentUnsavedDist = {1.0};

        for (int w = 0; w < woundDist.length; w++) {
            double prob = woundDist[w];
            if (prob > 0.0000001) {
                for (int i = 0; i < currentUnsavedDist.length; i++) totalUnsavedDist[i] += currentUnsavedDist[i] * prob;
            }
            if (w < woundDist.length - 1) {
                currentUnsavedDist = ProbabilityMath.convolve(currentUnsavedDist, singleWoundOutcome);
            }
        }
        return totalUnsavedDist;
    }

    /**
     * Wraps probability distributions into Graph Nodes for the frontend.
     */
    private CalculationResultDTO.ToughnessNode extractNodeStats(int toughness, double[] dist) {
        double[] stats = calculateStats(dist);
        return new CalculationResultDTO.ToughnessNode(toughness, stats[0], stats[1], stats[2]);
    }

    private CalculationResultDTO.SaveNode extractSaveNodeStats(String label, double[] dist) {
        double[] stats = calculateStats(dist);
        return new CalculationResultDTO.SaveNode(label, stats[0], stats[1], stats[2]);
    }

    /**
     * Extracts weighted average and 80% confidence interval from a distribution.
     */
    private double[] calculateStats(double[] dist) {
        double avg = 0.0, sumProb = 0.0, lower80 = -1.0, upper80 = -1.0;
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
     * Finalizes the DTO by enriching it with statistical metadata and rounding values for JSON transmission.
     */
    private CalculationResultDTO finalizeResults(List<CalculationRequestDTO> requests, double[] hitDist, double[] woundDist) {
        String damageExpression = requests.get(0).getDamageValue();
        double[] damageDist = DamageProcessor.calculateDamageDistribution(woundDist, damageExpression);

        CalculationResultDTO res = createBaseResult(hitDist);
        DistributionAnalyzer.enrichHits(res, hitDist);
        DistributionAnalyzer.enrichWounds(res, woundDist);
        DistributionAnalyzer.enrichDamage(res, damageDist);
        
        res.setWoundProbabilities(convertToRoundedList(woundDist));
        res.setDamageProbabilities(convertToRoundedList(damageDist));
        return res;
    }

    private boolean isRequestInvalid(List<CalculationRequestDTO> requests) {
        return requests == null || requests.isEmpty();
    }

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
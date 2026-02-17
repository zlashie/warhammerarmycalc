package com.warhammer.service;

import org.springframework.stereotype.Component;

/**
 * Encapsulates the statistical logic for D6-based trials.
 * Uses named constants to avoid magic numbers in probability formulas.
 */
@Component
public class ProbabilityEngine {

    private static final double DICE_FACES = 6.0;
    private static final double CHANCE_OF_SINGLE_FACE = 1.0 / DICE_FACES; 
    private static final double SUCCESS_THRESHOLD_OFFSET = 1.0; // Offset

    public enum RerollType { NONE, ONES, FAILED, ALL }

    public double calculateSuccessProb(int target, RerollType reroll) {
        double successfulOutcomes = DICE_FACES - target + SUCCESS_THRESHOLD_OFFSET;
        double baseProb = Math.max(0, Math.min(1.0, successfulOutcomes / DICE_FACES));
        
        return applyReroll(baseProb, reroll);
    }

    public double calculateCritProb(int threshold, RerollType reroll) {
        double criticalOutcomes = DICE_FACES - threshold + SUCCESS_THRESHOLD_OFFSET;
        double baseCritProb = criticalOutcomes / DICE_FACES;
        
        return applyReroll(baseCritProb, reroll);
    }

    private double applyReroll(double prob, RerollType reroll) {
        double failProb = 1.0 - prob;

        return switch (reroll) {
            case NONE -> prob;
            case ONES -> prob + (CHANCE_OF_SINGLE_FACE * prob);
            case FAILED, ALL -> prob + (failProb * prob); 
        };
    }
}
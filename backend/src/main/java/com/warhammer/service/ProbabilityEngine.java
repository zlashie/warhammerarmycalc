package com.warhammer.service;

import org.springframework.stereotype.Component;

@Component
public class ProbabilityEngine {

    private static final double DICE_FACES = 6.0;
    private static final double CHANCE_OF_SINGLE_FACE = 1.0 / DICE_FACES; 
    private static final double SUCCESS_THRESHOLD_OFFSET = 1.0;

    public enum RerollType { NONE, ONES, FAILED, ALL }

    /**
     * Calculates the probability of success, accounting for fishing logic.
     * @param target The BS/WS or Wound target (e.g., 3 for a 3+)
     * @param reroll The reroll strategy
     * @param critThreshold The value needed for a Critical (usually 6)
     */
    public double calculateSuccessProb(int target, RerollType reroll, int critThreshold) {
        double successfulOutcomes = DICE_FACES - target + SUCCESS_THRESHOLD_OFFSET;
        double baseProb = Math.max(0, Math.min(1.0, successfulOutcomes / DICE_FACES));
        
        if (reroll == RerollType.ALL) {
            double pCritInitial = (DICE_FACES - critThreshold + SUCCESS_THRESHOLD_OFFSET) / DICE_FACES;
            return pCritInitial + ((1.0 - pCritInitial) * baseProb);
        }

        return applyStandardReroll(baseProb, reroll);
    }

    public double calculateCritProb(int threshold, RerollType reroll) {
        double criticalOutcomes = DICE_FACES - threshold + SUCCESS_THRESHOLD_OFFSET;
        double baseCritProb = criticalOutcomes / DICE_FACES;
        
        if (reroll == RerollType.FAILED || reroll == RerollType.ALL) {
            return baseCritProb + ((1.0 - baseCritProb) * baseCritProb);
        }
        
        return applyStandardReroll(baseCritProb, reroll);
    }

    private double applyStandardReroll(double prob, RerollType reroll) {
        return switch (reroll) {
            case NONE -> prob;
            case ONES -> prob + (CHANCE_OF_SINGLE_FACE * prob);
            case FAILED -> prob + ((1.0 - prob) * prob);
            default -> prob;
        };
    }
}
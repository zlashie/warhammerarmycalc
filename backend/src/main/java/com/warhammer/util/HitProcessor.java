package com.warhammer.util;

import com.warhammer.dto.CalculationRequestDTO;

/**
 * Interprets game-specific rules and unit statistics to determine 
 * the probability distribution of successful hits.
 */
public class HitProcessor {

    private static final double DICE_FACES = 6.0;
    private static final double BS_BASE_TARGET = 7.0;

    /**
     * Translates a unit's attack profile and modifiers into a discrete 
     * probability distribution array.
     * * @param request The DTO containing model count, attacks, and BS value.
     * @return An array where index 'k' represents the probability of exactly 'k' hits.
     */
    public static double[] calculateUnitDistribution(CalculationRequestDTO request) {
        int totalAttacks = request.getNumberOfModels() * request.getAttacksPerModel();

        // Convert Ballistic Skill (e.g., 3+) into a raw success probability (p)
        double hitProbability = (BS_BASE_TARGET - request.getBsValue()) / DICE_FACES;
        hitProbability = Math.max(0, Math.min(1.0, hitProbability));

        return ProbabilityMath.calculateBinomial(totalAttacks, hitProbability);
    }
}
package com.warhammer.util;

import com.warhammer.dto.CalculationRequestDTO;

/**
 * Interprets game-specific rules (Sustained Hits, Ballistic Skill, etc.) to 
 * transform unit statistics into a discrete probability distribution.
 * * This class adheres to SOLID principles by isolating the business logic of 
 * Warhammer 40k mechanics from the underlying convolution mathematics 
 * handled by {@link ProbabilityMath}.
 */
public class HitProcessor {

    private static final int D6_SIDES = 6;
    private static final double PROB_PER_FACE = 1.0 / 6.0;

    /**
     * Calculates the total probability distribution for a unit's attack sequence.
     * * Uses iterative convolution to combine the outcomes of individual dice rolls
     * into a single array representing the unit's total hit potential.
     * * @param request The attack profile including model count, attacks, BS, and rules.
     * @return A double array where index 'k' is the probability of achieving exactly 'k' hits.
     */
    public static double[] calculateUnitDistribution(CalculationRequestDTO request) {
        int totalAttacks = request.getNumberOfModels() * request.getAttacksPerModel();
        
        if (totalAttacks <= 0) {
            return new double[]{1.0}; // 100% chance of 0 hits
        }

        double[] singleDieDist = buildSingleDieDistribution(request);
        double[] resultDistribution = {1.0};
        
        // Combine each individual die result into the total unit result
        for (int i = 0; i < totalAttacks; i++) {
            resultDistribution = ProbabilityMath.convolve(resultDistribution, singleDieDist);
        }

        return resultDistribution;
    }

    /**
     * Models the probability distribution of a single D6 roll based on BS and rules.
     * * Accounts for the core 40k rules:
     * - Natural 1s always fail.
     * - Natural 6s always hit.
     * - Sustained Hits generate extra hits (bonus) on a roll of 6.
     * * @param request The specific rules and modifiers for the attack.
     * @return An array representing outcomes {0 hits, 1 hit, 2 hits, ...}
     */
    private static double[] buildSingleDieDistribution(CalculationRequestDTO request) {
        // Determine the array size based on maximum possible hits from one die.
        // For Sustained D3, max is 4 (1 base hit + 3 bonus hits).
        int maxHitsFromOneDie = 1;
        if (request.isSustainedHits()) {
            if ("D3".equalsIgnoreCase(request.getSustainedValue())) {
                maxHitsFromOneDie = 4; 
            } else {
                try {
                    int bonus = Integer.parseInt(request.getSustainedValue());
                    maxHitsFromOneDie = 1 + bonus;
                } catch (Exception e) { 
                    maxHitsFromOneDie = 1; 
                }
            }
        }

        double[] dist = new double[maxHitsFromOneDie + 1]; 
        int bs = request.getBsValue();

        for (int face = 1; face <= D6_SIDES; face++) {
            if (face == 1) {
                dist[0] += PROB_PER_FACE; // Rule: Natural 1 always fails
            } else if (face == 6) {
                if (request.isSustainedHits()) {
                    handleSustainedEffect(dist, request.getSustainedValue());
                } else {
                    dist[1] += PROB_PER_FACE; // Rule: Natural 6 always hits
                }
            } else if (face >= bs) {
                dist[1] += PROB_PER_FACE; // Standard Success
            } else {
                dist[0] += PROB_PER_FACE; // Standard Fail
            }
        }
        return dist;
    }

    /**
     * Logic for handling Sustained Hits (exploding 6s).
     * * If SustainedValue is "D3", the bonus is 1, 2, or 3 hits (each 1/3 probability).
     * Adding the base hit results in a total of 2, 3, or 4 hits for that die.
     */
    private static void handleSustainedEffect(double[] dist, String value) {
        if ("D3".equalsIgnoreCase(value)) {
            // The 1/6 probability of rolling a '6' is split across the three D3 outcomes.
            double splitProb = PROB_PER_FACE / 3.0; 
            dist[2] += splitProb; // 1 base + 1 bonus
            dist[3] += splitProb; // 1 base + 2 bonus
            dist[4] += splitProb; // 1 base + 3 bonus
        } else {
            int bonus = 0;
            try {
                if (value != null) bonus = Integer.parseInt(value);
            } catch (NumberFormatException e) { 
                bonus = 0; 
            }
            
            int total = 1 + bonus;
            dist[total] += PROB_PER_FACE;
        }
    }
}
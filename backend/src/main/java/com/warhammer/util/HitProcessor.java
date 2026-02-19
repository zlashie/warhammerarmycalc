package com.warhammer.util;

import com.warhammer.dto.CalculationRequestDTO;

/**
 * The core logic engine for interpreting Warhammer 40,000 game mechanics.
 * * This class transforms high-level unit attributes (Ballistic Skill, Weapon Keywords) 
 * into discrete probability distributions. It serves as the bridge between 
 * tabletop rules and the mathematical convolution engine.
 */
public class HitProcessor {

    private static final int D6_SIDES = 6;
    private static final double PROB_PER_FACE = 1.0 / 6.0;

    /**
     * Generates a total probability distribution for a unit's entire attack sequence.
     * * It builds a distribution for a single die based on modifiers and iteratively 
     * convolved it with itself for every attack in the sequence.
     * @param request The unit's attack profile and active modifiers.
     * @return A double array where index {@code k} represents the probability of scoring exactly {@code k} hits.
     */
    public static double[] calculateUnitDistribution(CalculationRequestDTO request) {
        int totalAttacks = request.getNumberOfModels() * request.getAttacksPerModel();
        
        if (totalAttacks <= 0) {
            return new double[]{1.0}; // 100% chance of 0 hits
        }

        double[] singleDieDist = buildSingleDieDistribution(request);
        double[] resultDistribution = {1.0}; // Start state: 100% chance of 0 hits
        
        for (int i = 0; i < totalAttacks; i++) {
            resultDistribution = ProbabilityMath.convolve(resultDistribution, singleDieDist);
        }

        return resultDistribution;
    }

    /**
     * Constructs the probability outcomes for a single D6 roll, factoring in 
     * rerolls and critical hit effects.
     */
    private static double[] buildSingleDieDistribution(CalculationRequestDTO request) {
        // Index 0: Misses, 1: Standard Hits, 2-4: Possible Sustained Hit outcomes
        double[] dist = new double[5]; 
        int bs = request.getBsValue();

        for (int face = 1; face <= D6_SIDES; face++) {
            if (shouldReroll(face, bs, request)) {
                // If a reroll is triggered, the probability of this face (1/6) 
                // is redistributed across all 6 possible outcomes of the second roll.
                for (int rerollFace = 1; rerollFace <= D6_SIDES; rerollFace++) {
                    processFaceOutcome(rerollFace, dist, request, PROB_PER_FACE * PROB_PER_FACE);
                }
            } else {
                processFaceOutcome(face, dist, request, PROB_PER_FACE);
            }
        }
        return dist;
    }

    /**
     * Determines if a specific die face should be rerolled based on the unit's "RerollType".
     * Includes logic for "Fishing for Crits" (rerolling everything that isn't a 6).
     */
    private static boolean shouldReroll(int face, int bs, CalculationRequestDTO request) {
        return switch (request.getRerollType()) {
            case "ONES" -> face == 1;
            case "FAIL" -> face < bs || face == 1; 
            case "ALL" -> {
                            if (request.isSustainedHits()) {
                                yield face < request.getCritHitValue(); 
                            } else {
                                yield face < bs || face == 1;
                            }
                          }
            default -> false;
        };
    }

    /**
     * Maps a physical die face to a mathematical outcome in the distribution.
     * @param face The result of the D6 roll (1-6).
     * @param dist The distribution array to update.
     * @param request The source request for rule lookups.
     * @param probability The statistical weight of this specific outcome.
     */
    private static void processFaceOutcome(int face, double[] dist, CalculationRequestDTO request, double probability) {
        if (face == 1) {
            dist[0] += probability; 
        } else if (face >= request.getCritHitValue()) { // Changed from (face == 6)
            if (request.isSustainedHits()) {
                addSustainedToDist(dist, request.getSustainedValue(), probability);
            } else {
                dist[1] += probability; 
            }
        } else if (face >= request.getBsValue()) {
            dist[1] += probability; 
        } else {
            dist[0] += probability; 
        }
    }

    /**
     * Handles the "Sustained Hits" mechanic by assigning bonus hits to higher indices.
     * Supports static values (e.g., "1", "2") and variable "D3" explosions.
     */
    private static void addSustainedToDist(double[] dist, String value, double probability) {
        if ("D3".equalsIgnoreCase(value)) {
            // Split the 1/6 probability across the three possible D3 outcomes (1, 2, or 3 bonus).
            double split = probability / 3.0;
            dist[2] += split; // 1 base + 1 bonus
            dist[3] += split; // 1 base + 2 bonus
            dist[4] += split; // 1 base + 3 bonus
        } else {
            int bonus = 0;
            if (value != null && !value.isBlank()) {
                try {
                    bonus = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    bonus = 0; // Default to no bonus if string is malformed.
                }
            }
            
            // Total hits = 1 (base) + bonus.
            int targetIndex = 1 + bonus;
            if (targetIndex < dist.length) {
                dist[targetIndex] += probability;
            }
        }
    }
}
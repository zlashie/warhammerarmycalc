package com.warhammer.util;

/**
 * A immutable data container representing the partitioned probability distributions 
 * resulting from a wounding phase.
 * <p>
 * This record facilitates the "Save Scaling" logic by separating dice that require 
 * armor saves from those that bypass them.
 *
 * @param standardWounds    A probability distribution of wounds that proceed to the 
 *                          Saving Throw phase. This pool includes successful standard 
 *                          rolls and Lethal Hits.
 * @param devastatingWounds A probability distribution of wounds that triggered the 
 *                          'Devastating Wounds' rule. These dice skip the Saving 
 *                          Throw phase and are applied directly to the damage pool.
 * @param totalWounds       The consolidated distribution representing the sum of 
 *                          Standard and Devastating results. Used for baseline 
 *                          stat reporting and Toughness Scaling analysis.
 */
public record WoundResult(
    double[] standardWounds, 
    double[] devastatingWounds, 
    double[] totalWounds
) {}
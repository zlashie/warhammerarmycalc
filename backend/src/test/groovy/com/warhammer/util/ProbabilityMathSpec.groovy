package com.warhammer.util

import spock.lang.Specification
import spock.lang.Tag
import spock.lang.Title
import spock.lang.Unroll

/**
 * Technical Specification for the Probability Engine.
 * Validates the discrete mathematics used to simulate Warhammer 40,000 combat phases.
 */
@Title("Probability Math Engine Tests")
class ProbabilityMathSpec extends Specification {

    private static final double TOLERANCE = 0.000001

    // --- Combination & Selection Logic ---

    @Unroll
    def "calculateCombinations: should correctly count ways to choose #selectionCount from #totalTrials"() {
        expect: "Mathematical combinations (nCr) to match expected outcomes"
        ProbabilityMath.calculateCombinations(totalTrials, selectionCount) == expected

        where:
        totalTrials | selectionCount || expected
        6           | 0              || 1        // No successes possible
        6           | 1              || 6        // One success in six trials
        6           | 3              || 20       // "The middle" of the bell curve
        6           | 6              || 1        // All trials must succeed
        10          | 2              || 45       // Standard selection
        10          | 8              || 45       // Symmetry: 8 failures is 2 successes
        5           | 6              || 0        // Selection cannot exceed trials
        5           | -1             || 0        // Negative selection is invalid
        100         | 1              || 100      // Performance check for high volume
    }

    // --- Binomial Distribution Logic ---

    def "calculateBinomial: distribution for 10 attacks at BS 4+ should sum to 100%"() {
        given: "A standard attack scenario (10 trials, 50% success probability)"
        int totalAttacks = 10
        double hitProbability = 0.5

        when: "Generating the binomial distribution"
        double[] hitDistribution = ProbabilityMath.calculateBinomial(totalAttacks, hitProbability)

        then: "The total probability of all outcomes must equal 1.0 (100%)"
        Math.abs(hitDistribution.sum() - 1.0) < TOLERANCE
        
        and: "The distribution size must account for outcomes from 0 to N"
        hitDistribution.length == totalAttacks + 1
    }

    @Unroll
    def "calculateBinomial: should handle absolute success or failure: p=#successProbability"() {
        given: "A trial with guaranteed success or failure"
        int totalTrials = 5

        when: "Calculating the distribution"
        double[] distribution = ProbabilityMath.calculateBinomial(totalTrials, successProbability)

        then: "100% of the probability weight is concentrated at the expected outcome"
        distribution[expectedOutcomeIndex] == 1.0
        Math.abs(distribution.sum() - 1.0) < TOLERANCE

        where:
        successProbability || expectedOutcomeIndex
        0.0                || 0  // Torrent weapons failing (impossible) or 0% hit chance
        1.0                || 5  // Automatic hits/wounds
    }

    // --- Convolution Logic ---

    def "convolve: should merge two independent attack sources: Coin Flip Analogy"() {
        given: "Two independent single-attack sources with 50% hit chance"
        double[] attackSourceA = [0.5, 0.5] // Index 0: 50%, Index 1: 50%
        double[] attackSourceB = [0.5, 0.5]

        when: "Combining the sources into a unified army distribution"
        double[] totalDistribution = ProbabilityMath.convolve(attackSourceA, attackSourceB)

        then: "The result represents the probability of 0, 1, or 2 total hits"
        totalDistribution.length == 3
        totalDistribution[0] == 0.25 // Both fail (0.5 * 0.5)
        totalDistribution[1] == 0.50 // One succeeds (0.5*0.5 + 0.5*0.5)
        totalDistribution[2] == 0.25 // Both succeed (0.5 * 0.5)
    }

    def "convolve: should correctly apply a flat hit/damage bonus to an existing distribution"() {
        given: "A variable distribution (D6) and a guaranteed flat bonus (+2)"
        double[] d6Distribution = [0, 1/6, 1/6, 1/6, 1/6, 1/6, 1/6]
        double[] flatBonusOfTwo = [0, 0, 1.0] // 100% chance of adding exactly 2

        when: "Convolving the bonus into the dice roll"
        double[] result = ProbabilityMath.convolve(d6Distribution, flatBonusOfTwo)

        then: "The entire distribution should shift right by exactly 2 units"
        result.length == 9
        result[3] == (double)(1/6) // Original 1 becomes 3
        result[8] == (double)(1/6) // Original 6 becomes 8
        result[0..2].every { it == 0.0 } // No outcomes possible below 3
    }

    def "convolve: should maintain performance by skipping negligible probability branches"() {
        given: "A sparse distribution with zero-probability gaps"
        double[] sparseDistribution = [0.5, 0.0, 0.5]
        double[] identityDistribution = [1.0] // Probability of "0" extra hits is 100%

        when: "Combining the distributions"
        double[] result = ProbabilityMath.convolve(sparseDistribution, identityDistribution)

        then: "The result remains identical, confirming zero-weight outcomes were ignored"
        result == [0.5, 0.0, 0.5] as double[]
    }
}
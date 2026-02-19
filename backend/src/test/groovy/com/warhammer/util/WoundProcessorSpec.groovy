package com.warhammer.util

import spock.lang.Specification
import spock.lang.Unroll

class WoundProcessorSpec extends Specification {

    def "calculateWoundDistribution for a guaranteed hit outcome follows binomial distribution"() {
        given: "A distribution where we are 100% certain to get 4 hits"
        double[] hitDist = [0, 0, 0, 0, 1.0]
        
        when: "We calculate wounds requiring a 4+ (50% chance)"
        double[] woundDist = WoundProcessor.calculateWoundDistribution(hitDist, 4)

        then: "The max wounds equals max hits"
        woundDist.length == 5

        and: "The probability of 2 wounds (the peak) should be exactly 37.5%"
        // Binomial(n=4, k=2, p=0.5) = 6 * (0.5^2) * (0.5^2) = 0.375
        Math.abs(woundDist[2] - 0.375) < 0.0001
        
        and: "The total probability sums to 1.0"
        Math.abs(woundDist.sum() - 1.0) < 0.0001
    }

    @Unroll
    def "Average wounds should be #expectedAvg for #hits hits on a #target+ to wound"() {
        given: "A guaranteed number of hits"
        double[] hitDist = new double[hits + 1]
        hitDist[hits] = 1.0

        when:
        double[] woundDist = WoundProcessor.calculateWoundDistribution(hitDist, target)
        
        // Calculate the mean of the resulting distribution
        double actualAvg = 0
        woundDist.eachWithIndex { prob, i -> actualAvg += (i * prob) }

        then: "The average matches the mathematical expectation (hits * probability)"
        Math.abs(actualAvg - expectedAvg) < 0.0001

        where:
        hits | target | expectedAvg
        10   | 4      | 5.0          // 50% success
        10   | 2      | 8.3333       // 5/6 success
        10   | 6      | 1.6666       // 1/6 success
        1    | 4      | 0.5          // 1/2 success
    }

    def "Wound transformation should correctly weight a complex hit distribution"() {
        given: "A 50/50 distribution of 0 or 2 hits"
        double[] hitDist = [0.5, 0, 0.5]

        when: "Calculating wounds on a 4+"
        double[] woundDist = WoundProcessor.calculateWoundDistribution(hitDist, 4)

        then: "The logic uses the Law of Total Probability"
        // Scenario A (0 hits): 50% chance of 0 wounds
        // Scenario B (2 hits): 50% chance of [0.25, 0.50, 0.25] wounds
        // Total: 0 wounds: (0.5 + 0.5*0.25) = 0.625
        //        1 wound:  (0.5 * 0.5)     = 0.25
        //        2 wounds: (0.5 * 0.25)    = 0.125
        woundDist[0] == 0.625
        woundDist[1] == 0.25
        woundDist[2] == 0.125
    }

    def "Processor handles zero hits gracefully"() {
        given: "A distribution with only 0 hits"
        double[] hitDist = [1.0]

        when:
        double[] woundDist = WoundProcessor.calculateWoundDistribution(hitDist, 4)

        then:
        woundDist.length == 1
        woundDist[0] == 1.0
    }
}
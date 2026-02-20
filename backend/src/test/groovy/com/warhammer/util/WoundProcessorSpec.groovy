package com.warhammer.util

import com.warhammer.dto.CalculationRequestDTO
import spock.lang.Specification
import spock.lang.Unroll

class WoundProcessorSpec extends Specification {

    def defaultRequest = new CalculationRequestDTO(woundRerollType: "NONE")

    def "calculateWoundDistribution for a guaranteed hit outcome follows binomial distribution"() {
        given: "A distribution where we are 100% certain to get 4 hits"
        double[] hitDist = [0, 0, 0, 0, 1.0]
        
        when: "We calculate wounds requiring a 4+ (50% chance)"
        double[] woundDist = WoundProcessor.calculateWoundDistribution(hitDist, 4, defaultRequest)

        then: "The max wounds equals max hits"
        woundDist.length == 5

        and: "The probability of 2 wounds (the peak) should be exactly 37.5%"
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
        double[] woundDist = WoundProcessor.calculateWoundDistribution(hitDist, target, defaultRequest)
        
        double actualAvg = 0
        woundDist.eachWithIndex { prob, i -> actualAvg += (i * prob) }

        then: "The average matches the mathematical expectation (hits * probability)"
        Math.abs(actualAvg - expectedAvg) < 0.0001

        where:
        hits | target | expectedAvg
        10   | 4      | 5.0          
        10   | 2      | 8.3333      
        10   | 6      | 1.6666     
        1    | 4      | 0.5        
    }

    def "Wound transformation should correctly weight a complex hit distribution"() {
        given: "A 50/50 distribution of 0 or 2 hits"
        double[] hitDist = [0.5, 0, 0.5]

        when: "Calculating wounds on a 4+"
        double[] woundDist = WoundProcessor.calculateWoundDistribution(hitDist, 4, defaultRequest)

        then: "The logic uses the Law of Total Probability"
        woundDist[0] == 0.625
        woundDist[1] == 0.25
        woundDist[2] == 0.125
    }

    def "Processor handles zero hits gracefully"() {
        given: "A distribution with only 0 hits"
        double[] hitDist = [1.0]

        when:
        double[] woundDist = WoundProcessor.calculateWoundDistribution(hitDist, 4, defaultRequest)

        then:
        woundDist.length == 1
        woundDist[0] == 1.0
    }

    @Unroll
    def "Wound Rerolls: #type should result in avg #expectedAvg for 10 hits on 4+"() {
        given: "10 guaranteed hits"
        double[] hitDist = new double[11]
        hitDist[10] = 1.0
        def request = new CalculationRequestDTO(woundRerollType: type)

        when:
        double[] woundDist = WoundProcessor.calculateWoundDistribution(hitDist, 4, request)
        double actualAvg = 0
        woundDist.eachWithIndex { prob, i -> actualAvg += (i * prob) }

        then:
        Math.abs(actualAvg - expectedAvg) < 0.001

        where:
        type   | expectedAvg
        "ONES" | 5.8333      
        "FAIL" | 7.5         
        "ALL"  | 7.5         
    }

    def "Devastating Wounds with Reroll ALL should fish for crits"() {
        given: "6 guaranteed hits on a 4+ to wound"
        double[] hitDist = new double[7]
        hitDist[6] = 1.0

        def request = new CalculationRequestDTO(
            woundRerollType: "ALL", 
            devastatingWounds: true,
            critWoundValue: 6
        )

        when:
        double[] woundDist = WoundProcessor.calculateWoundDistribution(hitDist, 4, request)
        double actualAvg = 0
        woundDist.eachWithIndex { prob, i -> actualAvg += (i * prob) }

        then: "The math matches fishing logic"
        Math.abs(actualAvg - 3.5) < 0.001
    }

    def "Custom Crit Value should be respected"() {
        given: "10 hits on a 4+ to wound"
        double[] hitDist = new double[11]
        hitDist[10] = 1.0
        
        def request = new CalculationRequestDTO(
            woundRerollType: "NONE",
            devastatingWounds: false,
            critWoundValue: 5
        )

        when:
        double[] woundDist = WoundProcessor.calculateWoundDistribution(hitDist, 4, request)
        
        then: "Average should still be 5.0 (logic hasn't changed, just the 'label' of the die)"
        double actualAvg = 0
        woundDist.eachWithIndex { prob, i -> actualAvg += (i * prob) }
        Math.abs(actualAvg - 5.0) < 0.0001
    }
}
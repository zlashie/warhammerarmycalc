package com.warhammer.util

import com.warhammer.dto.CalculationRequestDTO
import spock.lang.Specification
import spock.lang.Unroll

class WoundProcessorSpec extends Specification {

    def defaultRequest = new CalculationRequestDTO(woundRerollType: "NONE")

    /**
     * Helper to reconcile the new 3-stream WoundResult with existing flat-array assertions.
     */
    private double[] getMergedWounds(double[] hits, int target, CalculationRequestDTO req) {
        WoundResult result = WoundProcessor.calculateWoundDistribution(hits, target, req)
        return result.totalWounds()
    }

    def "calculateWoundDistribution for a guaranteed hit outcome follows binomial distribution"() {
        given: "A distribution where we are 100% certain to get 4 hits"
        double[] hitDist = [0, 0, 0, 0, 1.0]
        
        when: "We calculate wounds requiring a 4+ (50% chance)"
        double[] woundDist = getMergedWounds(hitDist, 4, defaultRequest)

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
        double[] woundDist = getMergedWounds(hitDist, target, defaultRequest)
        
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
        double[] woundDist = getMergedWounds(hitDist, 4, defaultRequest)

        then: "The logic uses the Law of Total Probability"
        Math.abs(woundDist[0] - 0.625) < 0.0001
        Math.abs(woundDist[1] - 0.25) < 0.0001
        Math.abs(woundDist[2] - 0.125) < 0.0001
    }

    def "Processor handles zero hits gracefully"() {
        given: "A distribution with only 0 hits"
        double[] hitDist = [1.0]

        when:
        double[] woundDist = getMergedWounds(hitDist, 4, defaultRequest)

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
        double[] woundDist = getMergedWounds(hitDist, 4, request)
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
        double[] woundDist = getMergedWounds(hitDist, 4, request)
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
        double[] woundDist = getMergedWounds(hitDist, 4, request)
        
        then: "Average should still be 5.0 (logic hasn't changed, just the 'label' of the die)"
        double actualAvg = 0
        woundDist.eachWithIndex { prob, i -> actualAvg += (i * prob) }
        Math.abs(actualAvg - 5.0) < 0.0001
    }

    @Unroll
    def "Anti-X: #desc (Target #target+, Anti-#critValue+) should avg #expectedAvg wounds for 6 hits"() {
        given: "6 guaranteed hits"
        double[] hitDist = new double[7]
        hitDist[6] = 1.0
        
        def request = new CalculationRequestDTO(
            woundRerollType: "NONE",
            critWoundValue: critValue
        )

        when:
        double[] woundDist = getMergedWounds(hitDist, target, request)
        double actualAvg = 0
        woundDist.eachWithIndex { prob, i -> actualAvg += (i * prob) }

        then: "The average reflects that Anti-X provides a better success floor than the target roll"
        Math.abs(actualAvg - expectedAvg) < 0.001

        where:
        desc                     | target | critValue | expectedAvg
        "Standard (No Anti-X)"    | 4      | 6         | 3.0       
        "Anti-4+ vs High Tough"   | 6      | 4         | 3.0          
        "Anti-2+ vs High Tough"   | 5      | 2         | 5.0          
        "Crit overlaps target"    | 3      | 5         | 4.0        
    }

    def "Devastating Wounds with Reroll ALL, aka fishing, with Anti-4+"() {
        given: "6 guaranteed hits on a target that usually needs 6s to wound"
        double[] hitDist = new double[7]
        hitDist[6] = 1.0

        def request = new CalculationRequestDTO(
            woundRerollType: "ALL", 
            devastatingWounds: true,
            critWoundValue: 4    
        )

        when: "Calculating wounds against a 6+ target"
        double[] woundDist = getMergedWounds(hitDist, 6, request)
        double actualAvg = 0
        woundDist.eachWithIndex { prob, i -> actualAvg += (i * prob) }

        then: "Math: 50% chance on first roll, then reroll 50% of misses for another 50% success"
        Math.abs(actualAvg - 4.5) < 0.001
    }

    def "Anti-X should never succeed on a natural 1"() {
        given: "1 guaranteed hit and an impossible Anti-1+"
        double[] hitDist = [0, 1.0]
        def request = new CalculationRequestDTO(critWoundValue: 1, woundRerollType: "NONE")

        when: "Wounding on a 6+"
        double[] woundDist = getMergedWounds(hitDist, 6, request)

        then: "The success rate is 5/6 (2,3,4,5,6) because 1 is a hard-coded failure"
        Math.abs(woundDist[1] - 0.8333) < 0.001
    }

    def "Plus One to Wound should improve success rate but natural 1 still fails"() {
        given: "10 hits on a 4+ to wound with +1 modifier"
        double[] hitDist = new double[11]
        hitDist[10] = 1.0
        def request = new CalculationRequestDTO(plusOneToWound: true, woundRerollType: "NONE")

        when: "Calculating wounds"
        double[] woundDist = getMergedWounds(hitDist, 4, request)
        
        double actualAvg = 0
        woundDist.eachWithIndex { prob, i -> actualAvg += (i * prob) }

        then: "Success on natural 3, 4, 5, 6 (4/6 prob)."
        Math.abs(actualAvg - 6.6666) < 0.001
    }

    def "Devastating Wounds should siphon crits from standard success into the dev pool"() {
        given: "6 hits on a 4+ to wound with Devastating Wounds (Crit 6+)"
        double[] hitDist = new double[7]
        hitDist[6] = 1.0
        def request = new CalculationRequestDTO(devastatingWounds: true, critWoundValue: 6)

        when: "Calculating distribution"
        WoundResult result = WoundProcessor.calculateWoundDistribution(hitDist, 4, request)
        
        double stdAvg = 0
        result.standardWounds().eachWithIndex { p, i -> stdAvg += (i * p) }
        
        double devAvg = 0
        result.devastatingWounds().eachWithIndex { p, i -> devAvg += (i * p) }

        then: "Standard pool contains 4s and 5s (2/6 chance = 2.0 avg)"
        Math.abs(stdAvg - 2.0) < 0.001

        and: "Devastating pool contains 6s (1/6 chance = 1.0 avg)"
        Math.abs(devAvg - 1.0) < 0.001
        
        and: "The sum of averages equals the total success expectation (3.0)"
        Math.abs((stdAvg + devAvg) - 3.0) < 0.001
    }

    def "Anti-X 4+ with Devastating Wounds should siphon 50% of hits into the dev pool"() {
        given: "6 hits against a target that needs 6s, but with Anti-4+ and Dev Wounds"
        double[] hitDist = new double[7]
        hitDist[6] = 1.0
        def request = new CalculationRequestDTO(devastatingWounds: true, critWoundValue: 4)

        when: "Calculating distribution"
        WoundResult result = WoundProcessor.calculateWoundDistribution(hitDist, 6, request)
        
        double devAvg = 0
        result.devastatingWounds().eachWithIndex { p, i -> devAvg += (i * p) }
        
        double stdAvg = 0
        result.standardWounds().eachWithIndex { p, i -> stdAvg += (i * p) }

        then: "Every roll of 4, 5, or 6 is Devastating (3/6 chance = 3.0 avg)"
        Math.abs(devAvg - 3.0) < 0.001
        
        and: "There are no standard wounds because the crit threshold (4+) is lower than the target (6+)"
        Math.abs(stdAvg - 0.0) < 0.001
    }

    def "Lethal Hits should remain in the standard pool to allow saves"() {
        given: "A unit with 1 guaranteed Lethal Hit and 5 guaranteed Standard Hits"
        double[] lethalHits = new double[2] 
        lethalHits[1] = 1.0 

        double[] standardHits = new double[6]
        standardHits[5] = 1.0
        
        def request = new CalculationRequestDTO(devastatingWounds: true, critWoundValue: 6)

        when: "Calculating unit wounds (mirroring CalculatorService logic)"
        WoundResult res = WoundProcessor.calculateWoundDistribution(standardHits, 4, request)
        double[] finalStd = ProbabilityMath.convolve(res.standardWounds(), lethalHits)

        then: "The average reflects 5 hits rolling (4s and 5s succeed) + 1 auto-wound"
        double actualStdAvg = 0
        finalStd.eachWithIndex { p, i -> actualStdAvg += (i * p) }
        
        Math.abs(actualStdAvg - 2.6666) < 0.001

        and: "The Devastating pool only contains the crits from the 5 rolled hits"
        double actualDevAvg = 0
        res.devastatingWounds().eachWithIndex { p, i -> actualDevAvg += (i * p) }
        
        Math.abs(actualDevAvg - 0.8333) < 0.001
    }

    def "WoundProcessor should ensure single-die outcomes are collectively exhaustive"() {
        given: "A complex scenario with rerolls and modifiers"
        def request = new CalculationRequestDTO(
            woundRerollType: "FAIL", devastatingWounds: true, 
            plusOneToWound: true, critWoundValue: 5
        )

        when: "Calculating the outcome of a single die"
        double[] outcomes = new double[3]
        WoundProcessor.calculateSingleDieWound(outcomes, 4, request)

        then: "The sum of Fail + Standard + Devastating must be exactly 100%"
        Math.abs(outcomes.sum() - 1.0) < 0.0000001
    }

    def "Natural 6 should always wound even with negative modifiers"() {
        given: "A Strength 1 unit against Toughness 100 (Needs 6s), with a -1 to wound penalty"
        double[] hits = [0, 1.0] 
        def request = new CalculationRequestDTO(plusOneToWound: false)

        when: "Wounding on a 6+ (Natural 6 required)"
        WoundResult result = WoundProcessor.calculateWoundDistribution(hits, 7, request)

        then: "The natural 6 still succeeds because of the 'face == 6' safety check"
        Math.abs(result.totalWounds()[1] - 1/6.0) < 0.001
    }

    def "Plus One to Wound should not make natural 1s succeed"() {
        given: "1 guaranteed hit and +1 to wound"
        double[] hits = [0, 1.0]
        def request = new CalculationRequestDTO(plusOneToWound: true)

        when: "Wounding on a 2+"
        WoundResult result = WoundProcessor.calculateWoundDistribution(hits, 2, request)

        then: "Even though 1 + 1 = 2, the natural 1 must still fail"
        Math.abs(result.totalWounds()[1] - 5/6.0) < 0.001
    }
}
package com.warhammer.util

import com.warhammer.dto.CalculationRequestDTO
import spock.lang.Specification
import spock.lang.Unroll

class HitProcessorSpec extends Specification {

    /**
     * Helper to get the total combined hit distribution.
     * In the split-stream architecture, we convolve the Standard and Lethal 
     * results to find the total probability of X hits.
     */
    private double[] getTotalDist(CalculationRequestDTO request) {
        HitResult hitResult = HitProcessor.calculateUnitDistribution(request)
        return ProbabilityMath.convolve(hitResult.getStandardHits(), hitResult.getLethalHits())
    }

    @Unroll
    def "calculateUnitDistribution for BS #bs should have max hit potential of #expectedMaxIndex"() {
        given: "A request for a unit"
        def request = new CalculationRequestDTO(
            numberOfModels: models, attacksPerModel: attacks, bsValue: bs,
            sustainedHits: false, lethalHits: false
        )

        when: "The total distribution is calculated"
        double[] dist = getTotalDist(request)

        then: "The sum is 1.0 and the max index with probability is correct"
        Math.abs(dist.sum() - 1.0) < 0.000001
        int lastRealIndex = 0
        dist.eachWithIndex { val, idx -> if (val > 1e-10) lastRealIndex = idx }
        lastRealIndex == expectedMaxIndex

        where:
        models | attacks | bs || expectedMaxIndex
        10     | 1       | 4  || 10                
        1      | 20      | 3  || 20                
        5      | 2       | 2  || 10                
    }

    def "Sustained Hits should increase the maximum possible hits"() {
        given: "1 attack with Sustained 1"
        def request = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 4,
            sustainedHits: true, sustainedValue: "1", lethalHits: false
        )

        when:
        double[] dist = getTotalDist(request)

        then: "A single 4+ shot can result in 2 hits (1 base + 1 sustained)"
        dist.length >= 3
        Math.abs(dist[2] - 1/6.0) < 0.000001 
    }

    @Unroll
    def "Distribution for #models models, #attacks attacks, BS #bs, Sus:#susValue should have max #expectedMax"() {
        given: "A unit attack profile"
        def request = new CalculationRequestDTO(
            numberOfModels: models, 
            attacksPerModel: attacks, 
            bsValue: bs,
            sustainedHits: susActive,
            sustainedValue: susValue,
            lethalHits: false
        )

        when: "The distribution is calculated"
        double[] dist = getTotalDist(request)

        then: "The maximum possible outcome matches expectation"
        int lastRealIndex = 0
        dist.eachWithIndex { val, idx -> 
            if (val > 1e-11) lastRealIndex = idx 
        }
        lastRealIndex == expectedMax

        where:
        models | attacks | bs | susActive | susValue || expectedMax
        10     | 1       | 4  | false     | "0"      || 10 
        1      | 20      | 3  | false     | "0"      || 20
        1      | 1       | 4  | true      | "1"      || 2  
        1      | 1       | 4  | true      | "2"      || 3  
        1      | 1       | 4  | true      | "D3"     || 4  
        5      | 2       | 3  | true      | "1"      || 20 
    }

    def "Sustained D3 should distribute bonus hits across 2, 3, and 4 for a single 6"() {
        given: "1 attack that hits on 6+, with Sustained D3"
        def request = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 6,
            sustainedHits: true, sustainedValue: "D3", lethalHits: false
        )

        when:
        double[] dist = getTotalDist(request)

        then: "Probabilities should be split 1/18 each (1/6 for the 6, 1/3 for the D3)"
        Math.abs(dist[2] - 1/18.0) < 0.000001
        Math.abs(dist[3] - 1/18.0) < 0.000001
        Math.abs(dist[4] - 1/18.0) < 0.000001
    }

    def "Crit Hit modifier should trigger Sustained Hits on 5s if set to 5+"() {
        given: "BS 4+, Sustained 1, Crit Hit on 5+"
        def request = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 4,
            sustainedHits: true, sustainedValue: "1", critHitValue: 5, lethalHits: false
        )

        when:
        double[] dist = getTotalDist(request)

        then: "Faces 5 and 6 (2/6 prob) should result in 2 hits"
        Math.abs(dist[2] - 0.333333) < 0.0001
        and: "Face 4 (1/6 prob) should result in 1 hit"
        Math.abs(dist[1] - 0.166666) < 0.0001
    }

    def "Lethal Hits should move successful crits to the lethal stream"() {
        given: "1 attack, BS 4+, Lethal Hits active"
        def request = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 4,
            lethalHits: true, sustainedHits: false
        )

        when:
        HitResult result = HitProcessor.calculateUnitDistribution(request)

        then: "A '6' (1/6 prob) should be in the lethalHits[1] slot"
        Math.abs(result.lethalHits[1] - 1/6.0) < 0.000001
        
        and: "That same '6' should be a '0' in the standardHits pool to avoid double counting"
        Math.abs(result.standardHits[0] - 4/6.0) < 0.000001
        Math.abs(result.standardHits[1] - 2/6.0) < 0.000001
    }

    def "Lethal Hits on 5+ should increase auto-wound probability"() {
        given: "BS 4+, Lethal Hits, Crit on 5+"
        def request = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 4,
            lethalHits: true, critHitValue: 5
        )

        when:
        HitResult result = HitProcessor.calculateUnitDistribution(request)

        then: "Faces 5 and 6 (2/6 prob) should be lethal"
        Math.abs(result.lethalHits[1] - 2/6.0) < 0.000001
        and: "Only face 4 (1/6 prob) remains as a standard hit"
        Math.abs(result.standardHits[1] - 1/6.0) < 0.000001
    }

    def "Lethal and Sustained 1 together should generate both effects"() {
        given: "1 attack, Lethal Hits AND Sustained 1"
        def request = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 4,
            lethalHits: true, sustainedHits: true, sustainedValue: "1"
        )

        when:
        HitResult result = HitProcessor.calculateUnitDistribution(request)

        then: "The '6' creates 1 lethal hit AND 1 extra standard hit"
        Math.abs(result.lethalHits[1] - 1/6.0) < 0.000001
        
        and: "The explosion from Sustained Hits goes into the standard pool"
        Math.abs(result.standardHits[1] - 3/6.0) < 0.000001
    }

    def "Reroll ALL with Lethal Hits should 'Fish for Crits' correctly"() {
        given: "1 attack, BS 4+, Lethal Hits, Reroll ALL (Fishing)"
        def request = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 4,
            lethalHits: true, rerollType: "ALL"
        )

        when: "Calculating the lethal stream specifically"
        HitResult result = HitProcessor.calculateUnitDistribution(request)
        double lethalProb = result.lethalHits[1]

        then: "Probability of a 6 is (1/6) + (5/6 * 1/6) = 11/36"
        Math.abs(lethalProb - 11/36.0) < 0.000001
    }

    def "Plus One to Hit should improve success rate but natural 1 still fails"() {
        given: "BS 4+ with +1 modifier"
        def request = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 4,
            plusOneToHit: true
        )

        when: "Calculating hits"
        double[] dist = getTotalDist(request)

        then: "Hits on natural 3, 4, 5, 6 (4/6 prob). Natural 1 and 2 fail."
        Math.abs(dist[1] - 4/6.0) < 0.000001
        
        and: "The miss probability is 2/6"
        Math.abs(dist[0] - 2/6.0) < 0.000001
    }

    def "calculateUnitDistribution should handle variable attacks D3"() {
        given: "A single model with D3 attacks hitting on 4+"
        def request = new CalculationRequestDTO(
            numberOfModels: 1, 
            attacksPerModel: "D3", 
            bsValue: 4
        )

        when: "The total distribution is calculated"
        double[] dist = getTotalDist(request)
        double averageHits = dist.indexed().collect { i, p -> i * p }.sum()

        then: "Average attacks = 2. EV Hits = 2 * 0.5 = 1.0"
        Math.abs(averageHits - 1.0) < 0.0001
        
        and: "The max index with non-zero probability is 3"
        int lastRealIndex = 0
        dist.eachWithIndex { val, idx -> if (val > 1e-9) lastRealIndex = idx }
        lastRealIndex == 3
    }

    def "calculateUnitDistribution should handle complex attack expressions D6+2"() {
        given: "A single model with D6+2 attacks hitting on 3+"
        def request = new CalculationRequestDTO(
            numberOfModels: 1, 
            attacksPerModel: "D6+2", 
            bsValue: 3
        )

        when:
        double[] dist = getTotalDist(request)
        double averageHits = dist.indexed().collect { i, p -> i * p }.sum()

        then: "Average attacks = 5.5. EV Hits = 5.5 * (4/6) = 3.6667"
        Math.abs(averageHits - 3.66666) < 0.0001
        
        and: "The max index with probability is 8 (6+2)"
        int lastRealIndex = 0
        dist.eachWithIndex { val, idx -> if (val > 1e-9) lastRealIndex = idx }
        lastRealIndex == 8
    }

    def "calculateUnitDistribution should convolve multiple models with variable attacks"() {
        given: "2 models with D6 attacks hitting on 2+"
        def request = new CalculationRequestDTO(
            numberOfModels: 2, 
            attacksPerModel: "D6", 
            bsValue: 2
        )

        when:
        double[] dist = getTotalDist(request)
        double averageHits = dist.indexed().collect { i, p -> i * p }.sum()

        then: "Total expected attacks = 7. EV Hits = 7 * (5/6) = 5.8333"
        Math.abs(averageHits - 5.83333) < 0.0001
        
        and: "The max index with probability is 12 (2 * 6)"
        int lastRealIndex = 0
        dist.eachWithIndex { val, idx -> if (val > 1e-9) lastRealIndex = idx }
        lastRealIndex == 12
    }

    def "calculateUnitDistribution should remain backward compatible with flat integer strings"() {
        given: "A unit with '10' attacks as a string"
        def request = new CalculationRequestDTO(
            numberOfModels: 1, 
            attacksPerModel: "10", 
            bsValue: 4
        )

        when:
        double[] dist = getTotalDist(request)
        double averageHits = dist.indexed().collect { i, p -> i * p }.sum()

        then: "It treats '10' as a flat value. EV = 10 * 0.5 = 5.0"
        Math.abs(averageHits - 5.0) < 0.0001
    }
}
package com.warhammer.util

import com.warhammer.dto.CalculationRequestDTO
import spock.lang.Specification
import spock.lang.Unroll

class HitProcessorSpec extends Specification {

    @Unroll
    def "calculateUnitDistribution for BS #bs should have max hit potential of #expectedMaxIndex"() {
        given: "A request for a unit"
        def request = new CalculationRequestDTO(
            numberOfModels: models, 
            attacksPerModel: attacks, 
            bsValue: bs,
            sustainedHits: false
        )

        when: "The distribution is calculated"
        double[] dist = HitProcessor.calculateUnitDistribution(request)

        then: "The sum is 1.0 and the max index with probability is correct"
        Math.abs(dist.sum() - 1.0) < 0.000001
        int lastRealIndex = 0
        dist.eachWithIndex { val, idx -> if (val > 0.000001) lastRealIndex = idx }
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
            sustainedHits: true, sustainedValue: "1"
        )

        when:
        double[] dist = HitProcessor.calculateUnitDistribution(request)

        then: "A single 4+ shot can result in 2 hits"
        dist[2] > 0 
        Math.abs(dist[2] - 1/6) < 0.000001 
    }

    @Unroll
    def "Distribution for #models models, #attacks attacks, BS #bs, Sus:#susValue should have max #expectedMax"() {
        given: "A unit attack profile"
        def request = new CalculationRequestDTO(
            numberOfModels: models, 
            attacksPerModel: attacks, 
            bsValue: bs,
            sustainedHits: susActive,
            sustainedValue: susValue
        )

        when: "The distribution is calculated"
        double[] dist = HitProcessor.calculateUnitDistribution(request)

        then: "The sum of probabilities is always 100%"
        Math.abs(dist.sum() - 1.0) < 0.000001
        
        and: "The maximum possible outcome matches expectation"
        int lastRealIndex = 0
        dist.eachWithIndex { val, idx -> 
            if (val > 1e-15) lastRealIndex = idx 
        }
        lastRealIndex == expectedMax

        where:
        models | attacks | bs | susActive | susValue || expectedMax
        // --- Standard Shooting ---
        10     | 1       | 4  | false     | "0"      || 10  // 10 shots, max 1 per shot
        1      | 20      | 3  | false     | "0"      || 20
        // --- Sustained Hits (Exploding 6s) ---
        1      | 1       | 4  | true      | "1"      || 2   // 1 + 1 bonus
        1      | 1       | 4  | true      | "2"      || 3   // 1 + 2 bonus
        1      | 1       | 4  | true      | "D3"     || 4   // 1 + 3 bonus (max D3)
        5      | 2       | 3  | true      | "1"      || 20  // 10 shots * (1 base + 1 bonus) = 20
        // --- Edge Case BS ---
        1      | 10      | 1  | false     | "0"      || 10  // Potential max is 10 (on rolls of 2-6)
        1      | 10      | 7  | false     | "0"      || 10  // Potential max is 10 (on rolls of 6)
    }

    def "Sustained D3 should distribute bonus hits across 2, 3, and 4 for a single 6"() {
        given: "1 attack that hits on 6+, with Sustained D3"
        def request = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 6,
            sustainedHits: true, sustainedValue: "D3"
        )

        when:
        double[] dist = HitProcessor.calculateUnitDistribution(request)

        then: "Probabilities should be split (1/6 chance of 6, then 1/3 chance for each D3 value)"
        // P(2 hits) = P(roll 6) * P(D3=1) = 1/6 * 1/3 = 1/18
        // P(3 hits) = P(roll 6) * P(D3=2) = 1/6 * 1/3 = 1/18
        // P(4 hits) = P(roll 6) * P(D3=3) = 1/6 * 1/3 = 1/18
        Math.abs(dist[2] - 1/18.0) < 0.000001
        Math.abs(dist[3] - 1/18.0) < 0.000001
        Math.abs(dist[4] - 1/18.0) < 0.000001
    }

    def "Processor should handle zero attacks gracefully"() {
        given:
        def request = new CalculationRequestDTO(numberOfModels: 0, attacksPerModel: 10, bsValue: 4)

        when:
        double[] dist = HitProcessor.calculateUnitDistribution(request)

        then: "It returns a 100% chance of 0 hits"
        dist.length == 1
        dist[0] == 1.0
    }
}
package com.warhammer.service

import com.warhammer.dto.CalculationRequestDTO
import spock.lang.Specification

class CalculatorServiceSpec extends Specification {
    CalculatorService service = new CalculatorService()

    def "calculateArmyHits should correctly calculate hits considering 1s fail"() {
        given: "A unit with 3 attacks, BS 1 (effectively hitting on 2+)"
        def unit = new CalculationRequestDTO(numberOfModels: 1, attacksPerModel: 3, bsValue: 1, sustainedHits: false)

        when:
        def result = service.calculateArmyHits([unit])

        then: "Average should be 3 * (5/6) = 2.5"
        Math.abs(result.avgValue - 2.5) < 0.0001
    }

    def "calculateArmyHits should handle an empty list"() {
        expect:
        service.calculateArmyHits([]).avgValue == 0.0
    }

    def "calculateArmyHits should correctly merge distributions from different units"() {
        given: "One accurate unit and one inaccurate unit"
        def elite = new CalculationRequestDTO(numberOfModels: 1, attacksPerModel: 1, bsValue: 2) // 5/6 hits
        def recruit = new CalculationRequestDTO(numberOfModels: 1, attacksPerModel: 1, bsValue: 6) // 1/6 hits

        when:
        def result = service.calculateArmyHits([elite, recruit])

        then: "Total expected hits = 5/6 + 1/6 = 1.0"
        Math.abs(result.avgValue - 1.0) < 0.0001
    }

    def "calculateArmyHits should integrate Sustained Hits from multiple units"() {
        given: "A unit with Sustained Hits 1"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 6, 
            sustainedHits: true, sustainedValue: "1"
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "On a 6, it deals 2 hits. Prob(6) = 1/6. Expected value = 2 * (1/6) = 0.3333"
        Math.abs(result.avgValue - 0.333333) < 0.0001
    }

    def "calculateArmyHits should treat null sustainedValue as Sustained 0"() {
        given: "Sustained is active but value is null"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 10, bsValue: 4, 
            sustainedHits: true, sustainedValue: null
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "It shouldn't crash and should treat it as 0 bonus hits (50% hit rate)"
        Math.abs(result.avgValue - 5.0) < 0.0001
    }

    def "calculateArmyHits should correctly integrate reroll mechanics"() {
        given: "A unit with 10 attacks, BS 4+, rerolling 1s"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 10, bsValue: 4, 
            rerollType: "ONES", sustainedHits: false
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "Expected average is 10 * 0.58333 = 5.8333"
        Math.abs(result.avgValue - 5.8333) < 0.0001
    }

    def "calculateArmyHits should process the full chain including D6 damage"() {
        given: "A single model with 1 attack, hitting on 2+, wounding on 4+, with D6 damage"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, 
            attacksPerModel: 1, 
            bsValue: 2, 
            damageValue: "D6"
        )

        when: "The service runs the calculation"
        def result = service.calculateArmyHits([unit])

        then: "The math should check out"
        // Hit Average: 1 * 5/6 = 0.8333
        Math.abs(result.avgValue - 0.8333) < 0.001
        
        // Wound Average: 0.8333 * 0.5 = 0.4166
        Math.abs(result.woundAvgValue - 0.4166) < 0.001
        
        // Damage Average: 0.4166 * 3.5 (avg of D6) = 1.4583
        Math.abs(result.damageAvgValue - 1.4583) < 0.001

        and: "The damage statistical fields should be populated"
        result.damageRange80 != null
        result.damageProbabilities.size() > 1
    }
}
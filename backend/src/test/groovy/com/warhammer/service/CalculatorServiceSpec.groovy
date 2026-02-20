package com.warhammer.service

import com.warhammer.dto.CalculationRequestDTO
import spock.lang.Specification

class CalculatorServiceSpec extends Specification {
    CalculatorService service = new CalculatorService()

    def "calculateArmyHits should correctly calculate hits considering 1s fail"() {
        given: "A unit with 3 attacks, BS 1 (effectively hitting on 2+)"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, 
            attacksPerModel: 3, 
            bsValue: 1, 
            sustainedHits: false, 
            lethalHits: false
        )

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
        def elite = new CalculationRequestDTO(numberOfModels: 1, attacksPerModel: 1, bsValue: 2) 
        def recruit = new CalculationRequestDTO(numberOfModels: 1, attacksPerModel: 1, bsValue: 6) 

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

        then: "On a 6 (1/6 prob), it deals 2 hits (1 base + 1 bonus). EV = 2 * (1/6) = 0.3333"
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

        then: "It treats null as 0 bonus hits (50% hit rate for 10 attacks)"
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

        then: "Expected average is 10 * (0.5 + (1/6 * 0.5)) = 5.8333"
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

        when:
        def result = service.calculateArmyHits([unit])

        then: "The math for the full chain should be correct"
        Math.abs(result.avgValue - 0.8333) < 0.001
        Math.abs(result.woundAvgValue - 0.4166) < 0.001
        Math.abs(result.damageAvgValue - 1.4583) < 0.001

        and: "The damage statistical fields should be populated"
        result.damageRange80 != null
        result.damageProbabilities.size() > 1
    }

    def "calculateArmyHits should correctly bypass wound roll for Lethal Hits"() {
        given: "A single attack with Lethal Hits that hits on 6+"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 6, 
            lethalHits: true, sustainedHits: false
        )

        when: "Calculating the army distribution"
        def result = service.calculateArmyHits([unit])

        then: "The average wounds should be exactly the hit probability (1/6)"
        Math.abs(result.woundAvgValue - 0.166666) < 0.0001
        
        and: "If it were NOT lethal, EV would be 1/6 (hit) * 1/2 (wound) = 0.0833"
        result.woundAvgValue > 0.0833 
    }

    def "calculateArmyHits should handle the Lethal + Sustained interaction pipeline"() {
        given: "1 attack, Lethal and Sustained 1, hitting on 6+"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 6, 
            lethalHits: true, sustainedHits: true, sustainedValue: "1"
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "Math: (1/6 lethal) + (1/6 hit * 1/2 wound chance) = 0.25"
        Math.abs(result.woundAvgValue - 0.25) < 0.0001
    }

    def "Army convolution should merge units with and without Lethal Hits"() {
        given: "Unit A (Lethal) and Unit B (Standard)"
        def lethalUnit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 6, lethalHits: true 
        ) 
        
        def standardUnit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 6, lethalHits: false
        ) 

        when:
        def result = service.calculateArmyHits([lethalUnit, standardUnit])

        then: "Total wounds should be the sum of both (0.1666 + 0.0833 = 0.25)"
        Math.abs(result.woundAvgValue - 0.25) < 0.0001
    }

    def "calculateArmyHits should correctly apply damage to the combined wound pool"() {
        given: "A unit where ALL hits are critical (and thus lethal)"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 2, 
            lethalHits: true, critHitValue: 2, 
            damageValue: "3"
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "5/6 hits are lethal -> 0.8333 wounds * 3 damage = 2.5"
        Math.abs(result.damageAvgValue - 2.5) < 0.001 
    }

    def "calculateArmyHits should integrate Wound Reroll FAIL mechanics"() {
        given: "A unit with 10 hits, wounding on 4+, rerolling fails"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 12, bsValue: 2, 
            woundRerollType: "FAIL"
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "Math: 10 hits * (0.5 base + (0.5 fail * 0.5 reroll success)) = 7.5 expected wounds"
        Math.abs(result.woundAvgValue - 7.5) < 0.001
    }

    def "calculateArmyHits should handle Devastating Wounds and Reroll ALL, aka Fishing for Crits)"() {
        given: "1 attack, wounding on 4+, Devastating Wounds and Reroll ALL"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 1, bsValue: 2,
            woundRerollType: "ALL", 
            devastatingWounds: true,
            critWoundValue: 6
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "Math: Single die success is 21/36 (0.5833). Service should reflect this."
        Math.abs(result.woundAvgValue - 0.4861) < 0.001
    }
}
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

    def "calculateArmyHits should handle Devastating Wounds and Reroll ALL , aka fishing for Criticals"() {
        given: "A unit profile optimized for Devastating Wounds (BS 2+, Wounding 4+, Crit 6+, Reroll ALL)"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, 
            attacksPerModel: 1, 
            bsValue: 2,
            woundRerollType: "ALL", 
            devastatingWounds: true,
            critWoundValue: 6
        )

        when: "The service processes the attack through the hit and wound pipeline"
        def result = service.calculateArmyHits([unit])

        then: "The average wounds matches fishing logic: Hit(5/6) * P(Wound with Fishing)"
        Math.abs(result.woundAvgValue - 0.4861) < 0.001
    }

    def "calculateArmyHits should correctly apply +1 to hit and +1 to wound modifiers"() {
        given: "A unit with BS 4+ and Wounding on 4+ (hardcoded in service), with +1 to both rolls"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, 
            attacksPerModel: 10, 
            bsValue: 4, 
            plusOneToHit: true,   
            plusOneToWound: true,
            damageValue: "1" 
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "Expected Hits: 10 * (4/6) = 6.6667"
        Math.abs(result.avgValue - 6.6667) < 0.001

        and: "Expected Wounds: 6.6667 * (4/6) = 4.4444"
        Math.abs(result.woundAvgValue - 4.4444) < 0.001
    }

    def "calculateArmyHits should correctly populate Save Scaling graph data"() {
        given: "A standard unit (10 attacks, 3+ hit, 4+ wound, 1 damage, AP 0)"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 10, bsValue: 3, 
            strength: 4, ap: 0, damageValue: "1"
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "The save scaling list should contain exactly 6 nodes (2+ through 7/None)"
        result.saveScaling.size() == 6
        result.saveScaling[0].saveLabel == "2+"
        result.saveScaling[5].saveLabel == "None"

        and: "Against 'None' (index 5), average damage should equal average wounds (~3.33)"
        Math.abs(result.saveScaling[5].average - 3.3333) < 0.01
        
        and: "Against a 2+ save (index 0), average damage should be roughly 1/6th of total (~0.55)"
        Math.abs(result.saveScaling[0].average - 0.5555) < 0.01
    }

    def "Save Scaling should correctly respect AP"() {
        given: "A high AP unit (10 attacks, 3+ hit, 4+ wound, 1 damage, AP -3)"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 10, bsValue: 3, 
            strength: 4, ap: 3, damageValue: "1"
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "Against a 2+ save, AP -3 forces a 5+ save (50% fail chance)"
        Math.abs(result.saveScaling[0].average - 2.2222) < 0.01
        
        and: "Against a 4+ save, AP -3 makes it impossible (None/7+)"
        Math.abs(result.saveScaling[2].average - 3.3333) < 0.01
    }

    def "Save Scaling should ensure a 1 always fails regardless of AP"() {
        given: "A unit with high AP but the target has a 2+ save"

        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 6, bsValue: 2, 
            strength: 4, ap: 10, damageValue: "1"
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "At AP -10, even a 2+ save profile should take max damage (relative to the T4 wounding baseline)"
        Math.abs(result.saveScaling[0].average - 2.5) < 0.01
    }

    def "Save Scaling should be agnostic to the sign of the AP value"() {
        given: "Two units with identical stats but opposite AP signs"
        def unitPos = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 12, bsValue: 3, 
            strength: 4, ap: 2, damageValue: "1"
        )
        def unitNeg = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 12, bsValue: 3, 
            strength: 4, ap: -2, damageValue: "1"
        )

        when: "Calculating performance for both"
        def resPos = service.calculateArmyHits([unitPos])
        def resNeg = service.calculateArmyHits([unitNeg])

        then: "Both should produce the exact same average damage scaling"
        resPos.saveScaling.collect { it.average } == resNeg.saveScaling.collect { it.average }
    }

    def "Save Scaling should correctly handle the Devastating Wounds damage floor"() {
        given: "A unit with 6 attacks, 2+ hit, 4+ wound, 1 damage, and Devastating Wounds (Crit 6+)"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 6, bsValue: 2, 
            strength: 4, ap: 0, damageValue: "1",
            devastatingWounds: true, critWoundValue: 6
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "Against 'None' (index 5), avg damage is total wounds (0.833 + 1.666 = 2.5)"
        Math.abs(result.saveScaling[5].average - 2.5) < 0.01

        and: "Against a 2+ save (index 0), standard wounds are reduced by 5/6, but Devastating are not"
        Math.abs(result.saveScaling[0].average - 1.111) < 0.01
    }

    def "Toughness Scaling should reflect Strength vs Toughness breakpoints"() {
        given: "A Strength 4 weapon"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 6, bsValue: 2, 
            strength: 4, damageValue: "1"
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "Breakpoints should be visible in the scaling data"
        Math.abs(result.toughnessScaling.find { it.toughness == 2 }.average - 4.1666) < 0.1
        Math.abs(result.toughnessScaling.find { it.toughness == 4 }.average - 2.5) < 0.1
        Math.abs(result.toughnessScaling.find { it.toughness == 8 }.average - 0.833) < 0.1
    }

    def "Torrent weapons should hit automatically regardless of BS"() {
        given: "A unit with Torrent and a terrible BS 6+"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 10, bsValue: 6, 
            torrent: true
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "Average hits should be exactly 10 (100% hit rate)"
        result.avgValue == 10.0
    }

    def "Anti-X should provide consistent wounding regardless of target toughness"() {
        given: "Strength 1 weapon with Anti-Vehicle 4+ against T12"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 6, bsValue: 2, 
            strength: 1, critWoundValue: 4
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "The average wounds should reflect a 4+ success (3/6 chance), not a 6+"
        Math.abs(result.woundAvgValue - 2.5) < 0.01
    }

    def "Save Scaling should correctly handle armor boundaries with absolute AP"() {
        given: "A weapon with 6 attacks (5 hits), wounding on 4+ (2.5 wounds), AP -1"
        def unit = new CalculationRequestDTO(
            numberOfModels: 1, attacksPerModel: 6, bsValue: 2, 
            strength: 4, ap: -1, damageValue: "1" 
        )

        when:
        def result = service.calculateArmyHits([unit])

        then: "A 2+ save becomes a 3+ save (1/3 failure rate)"
        Math.abs(result.saveScaling[0].average - 0.8333) < 0.01

        and: "A 6+ save with AP -1 (absolute 1) becomes a 7+ (100% fail rate)"
        Math.abs(result.saveScaling[4].average - 2.5) < 0.01
        
        and: "The 'None' node always shows full damage regardless of AP"
        Math.abs(result.saveScaling[5].average - 2.5) < 0.01
    }
}
package com.warhammer.service

import com.warhammer.dto.CalculationRequestDTO
import com.warhammer.dto.CalculationResultDTO
import spock.lang.Specification

class CalculatorServiceSpec extends Specification {

    CalculatorService service = new CalculatorService()

    def "calculateArmyHits should correctly merge multiple units into one result"() {
        given: "Two units with guaranteed hits for easy verification"
        // Unit 1: 1 model, 1 attack, BS 1+ (Formula: 7-1/6 = 6/6 = 100% hit)
        def unit1 = new CalculationRequestDTO(numberOfModels: 1, attacksPerModel: 1, bsValue: 1)
        // Unit 2: 1 model, 2 attacks, BS 1+ (100% hit)
        def unit2 = new CalculationRequestDTO(numberOfModels: 1, attacksPerModel: 2, bsValue: 1)

        when: "The service calculates the army total"
        CalculationResultDTO result = service.calculateArmyHits([unit1, unit2])

        then: "The total max hits should be 3 (1 from unit1 + 2 from unit2)"
        result.maxHits == 3
        
        and: "The average value should be exactly 3.0"
        result.avgValue == 3.0

        and: "The probability list should reflect 100% chance of 3 hits"
        // Index 0, 1, 2 should be 0.0. Index 3 should be 1.0.
        result.probabilities[3] == 1.0
    }

    def "calculateArmyHits should handle an empty list of requests"() {
        when: "An empty army is submitted"
        CalculationResultDTO result = service.calculateArmyHits([])

        then: "It should return the initial state (100% chance of 0 hits)"
        result.maxHits == 0
        result.avgValue == 0.0
        result.probabilities == [1.0]
    }
}
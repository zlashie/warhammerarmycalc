package com.warhammer.service.rules

import com.warhammer.dto.UnitTogglesDTO
import com.warhammer.model.AttackResult
import com.warhammer.service.ProbabilityEngine
import com.warhammer.service.ProbabilityEngine.RerollType
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class RerollWoundsRuleSpec extends BaseRuleSpec {

    @Subject
    RerollWoundsRule rule = new RerollWoundsRule()
    
    ProbabilityEngine engine = Mock(ProbabilityEngine)

    def "isApplicable returns true when rerollWounds is not null"() {
        given: "A toggle record"
        def toggles = new UnitTogglesDTO(false, null, null, null, rerollValue, null, false)

        expect:
        rule.isApplicable(toggles) == expected

        where:
        rerollValue | expected
        "1s"        | true
        "FAIL"      | true
        null        | false
    }

    @Unroll
    def "apply maps '#input' string to RerollType.#expectedType for wounds"() {
        given: "An initial AttackResult with no rerolls"
        def initialResult = new AttackResult(
            10.0,            // attacksMean
            0.0,             // attacksVariance
            10.0,            // hitPool
            0.0,             // autoWounds
            0.0,             // damageMean
            0.0,             // damageVariance
            6,               // critThreshold
            RerollType.NONE, // hitReroll
            6,               // critWoundThreshold
            RerollType.NONE, // woundReroll
            0.0              // finalWounds
        )
        
        and: "Toggles with a specific wound reroll string"
        def toggles = new UnitTogglesDTO(false, null, null, null, input, null, false)

        when: "The rule is applied"
        def finalResult = rule.apply(initialResult, toggles, engine)

        then: "The record is updated with the correct woundReroll enum type"
        finalResult.woundReroll() == expectedType
        
        and: "The hitReroll remains unchanged"
        finalResult.hitReroll() == RerollType.NONE

        where:
        input  | expectedType
        "1s"   | RerollType.ONES
        "FAIL" | RerollType.FAILED
        "ALL"  | RerollType.ALL
        "none" | RerollType.NONE
    }
}
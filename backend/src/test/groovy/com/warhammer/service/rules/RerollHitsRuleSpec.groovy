package com.warhammer.service.rules

import com.warhammer.dto.UnitTogglesDTO
import com.warhammer.model.AttackResult
import com.warhammer.service.ProbabilityEngine
import com.warhammer.service.ProbabilityEngine.RerollType
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class RerollHitsRuleSpec extends BaseRuleSpec {

    @Subject
    RerollHitsRule rule = new RerollHitsRule()
    
    ProbabilityEngine engine = Mock(ProbabilityEngine)

    def "isApplicable returns true when rerollHits is not null"() {
        given: "A toggle record"
        def toggles = new UnitTogglesDTO(false, null, null, rerollValue, null, null, false)

        expect:
        rule.isApplicable(toggles) == expected

        where:
        rerollValue | expected
        "1s"        | true
        "FAIL"      | true
        null        | false
    }

    @Unroll
    def "apply maps '#input' string to RerollType.#expectedType"() {
        given: "An initial AttackResult with no rerolls"

        def initialResult = new AttackResult(
            10.0,                   // attacksMean
            0.0,                    // attacksVariance
            10.0,                   // hitPool
            0.0,                    // autoWounds
            0.0,                    // damageMean
            0.0,                    // damageVariance
            6,                      // critThreshold
            RerollType.NONE,        // hitReroll
            6,                      // critWoundThreshold
            RerollType.NONE,        // woundReroll
            0.0                     // finalWounds
        )
        
        and: "Toggles with a specific reroll string"
        def toggles = new UnitTogglesDTO(false, null, null, input, null, null, false)

        when: "The rule is applied"
        def finalResult = rule.apply(initialResult, toggles, engine)

        then: "The record is updated with the correct enum type"
        finalResult.hitReroll() == expectedType
        
        and: "The rest of the record state remains unchanged"
        finalResult.attacksMean() == 10.0

        where:
        input  | expectedType
        "1s"   | RerollType.ONES
        "fail" | RerollType.FAILED
        "ALL"  | RerollType.ALL
        "none" | RerollType.NONE
        "XYZ"  | RerollType.NONE
    }
}
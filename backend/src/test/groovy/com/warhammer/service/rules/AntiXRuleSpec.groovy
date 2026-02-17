package com.warhammer.service.rules

import com.warhammer.dto.UnitTogglesDTO
import com.warhammer.model.AttackResult
import com.warhammer.service.ProbabilityEngine
import com.warhammer.service.ProbabilityEngine.RerollType
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class AntiXRuleSpec extends BaseRuleSpec {

    @Subject
    AntiXRule rule = new AntiXRule()
    
    ProbabilityEngine engine = Mock(ProbabilityEngine)

    def "isApplicable returns true when antiX is provided and not blank"() {
        given: "A toggle record"
        def toggles = new UnitTogglesDTO(false, null, null, null, null, antiXValue, false)

        expect:
        rule.isApplicable(toggles) == expected

        where:
        antiXValue | expected
        "4+"       | true
        "2"        | true
        ""         | false
        null       | false
    }

    @Unroll
    def "apply parses '#input' and sets critWoundThreshold to #expectedThreshold"() {
        given: "An initial AttackResult with default 6+ wound crit threshold"
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
        
        and: "Toggles with an Anti-X string"
        def toggles = new UnitTogglesDTO(false, null, null, null, null, input, false)

        when: "The rule is applied"
        def finalResult = rule.apply(initialResult, toggles, engine)

        then: "The critWoundThreshold is updated while the hit critThreshold remains 6"
        finalResult.critWoundThreshold() == expectedThreshold
        finalResult.critThreshold() == 6

        where:
        input     | expectedThreshold
        "4+"      | 4
        "2+"      | 2
        "Vehicle" | 6  // Fallback to DEFAULT_CRIT if no digits found
    }
}
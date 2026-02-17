package com.warhammer.service.rules

import com.warhammer.dto.UnitTogglesDTO
import com.warhammer.model.AttackResult
import com.warhammer.service.ProbabilityEngine
import com.warhammer.service.ProbabilityEngine.RerollType
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class CritThresholdRuleSpec extends BaseRuleSpec {

    @Subject
    CritThresholdRule rule = new CritThresholdRule()
    
    ProbabilityEngine engine = Mock(ProbabilityEngine)

    def "isApplicable returns true when crit is not null and not blank"() {
        given: "A toggle record"
        def toggles = new UnitTogglesDTO(false, null, critValue, null, null, null, false)

        expect:
        rule.isApplicable(toggles) == expected

        where:
        critValue | expected
        "5+"      | true
        "6"       | true
        ""        | false
        "  "      | false
        null      | false
    }

    @Unroll
    def "apply parses '#input' and sets critThreshold to #expectedThreshold"() {
        given: "An initial AttackResult with the default 6+ threshold"
        def initialResult = new AttackResult(
            10.0, 0.0, 10.0, 0.0, 0.0, 0.0, 
            6, RerollType.NONE, 6, RerollType.NONE, 0.0
        )
        
        and: "Toggles with a specific crit string"
        def toggles = new UnitTogglesDTO(false, null, input, null, null, null, false)

        when: "The rule is applied"
        def finalResult = rule.apply(initialResult, toggles, engine)

        then: "The record threshold is updated correctly"
        finalResult.critThreshold() == expectedThreshold

        where:
        input  | expectedThreshold
        "5+"   | 5
        "4"    | 4
        "crit" | 6  
        ""     | 6  
    }
}
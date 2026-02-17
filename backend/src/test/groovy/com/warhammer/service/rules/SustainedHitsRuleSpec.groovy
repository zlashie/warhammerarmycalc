package com.warhammer.service.rules

import com.warhammer.dto.UnitTogglesDTO
import com.warhammer.model.AttackResult
import com.warhammer.service.ProbabilityEngine
import com.warhammer.service.ProbabilityEngine.RerollType
import spock.lang.Specification
import spock.lang.Subject

class SustainedHitsRuleSpec extends BaseRuleSpec {

    @Subject
    SustainedHitsRule rule = new SustainedHitsRule()
    
    ProbabilityEngine engine = Mock(ProbabilityEngine)

    def "isApplicable returns true only when sustained toggle is not null"() {
        given: "A toggle record with the sustained field populated"
        def toggles = new UnitTogglesDTO(false, sustainedValue, null, null, null, null, false)

        expect:
        rule.isApplicable(toggles) == expected

        where:
        sustainedValue | expected
        1              | true
        2              | true
        null           | false
    }

    def "apply adds extra hits to the hit pool without increasing autoWounds"() {
        given: "An initial AttackResult with 10 attacks and a 6+ crit threshold"
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
        
        def toggles = new UnitTogglesDTO(false, 2, null, null, null, null, false)

        and: "The engine returns a 1/6 (0.1667) critical hit probability"
        engine.calculateCritProb(6, RerollType.NONE) >> 0.1667

        when: "The rule is applied"
        def finalResult = rule.apply(initialResult, toggles, engine)

        then: "Extra hits = attacksMean * probCrit * sustainedValue"
        // 10 * 0.1667 * 2 = 3.334
        // The hitPool should increase by this amount
        finalResult.hitPool() == 13.334
        
        and: "AutoWounds should remain unchanged"
        finalResult.autoWounds() == 0.0
    }
}
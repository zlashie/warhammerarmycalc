package com.warhammer.service.rules

import com.warhammer.dto.UnitTogglesDTO
import com.warhammer.model.AttackResult
import com.warhammer.service.ProbabilityEngine
import com.warhammer.service.ProbabilityEngine.RerollType
import spock.lang.Specification
import spock.lang.Subject

class LethalHitsRuleSpec extends BaseRuleSpec {

    @Subject
    LethalHitsRule rule = new LethalHitsRule()
    
    ProbabilityEngine engine = Mock(ProbabilityEngine)

    def "isApplicable returns true only when lethal toggle is true"() {
        given: "A toggle record with the lethal flag set"
        def toggles = new UnitTogglesDTO(lethalActive, null, null, null, null, null, false)

        expect:
        rule.isApplicable(toggles) == expected

        where:
        lethalActive | expected
        true         | true
        false        | false
    }

    def "apply generates auto-wounds and reduces the hit pool based on engine math"() {
        given: "An initial AttackResult with 10 attacks and a 6+ crit threshold"
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
        
        def toggles = new UnitTogglesDTO(true, null, null, null, null, null, false)

        and: "The engine returns a 10% critical hit probability"
        engine.calculateCritProb(6, RerollType.NONE) >> 0.1

        when: "The rule is applied"
        def finalResult = rule.apply(initialResult, toggles, engine)

        then: "10.0 attacks * 0.1 prob = 1.0 autoWounds"
        finalResult.autoWounds() == 1.0
        
        and: "The hit pool is reduced by the amount transitioned to auto-wounds"
        finalResult.hitPool() == 9.0
    }
}
package com.warhammer.service.rules

import com.warhammer.dto.UnitTogglesDTO
import com.warhammer.model.AttackResult
import com.warhammer.service.ProbabilityEngine
import com.warhammer.service.ProbabilityEngine.RerollType
import spock.lang.Specification

abstract class BaseRuleSpec extends Specification {

    abstract AttackRule getRule()

    def "all rules must return a new AttackResult and not modify the original"() {
        given: "A rule and an initial state"
        def initial = new AttackResult(
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

        def toggles = new UnitTogglesDTO(true, 1, "6", "ALL", "ALL", "4+", true)
        def engine = Mock(ProbabilityEngine)

        when: "The rule is applied"
        def result = getRule().apply(initial, toggles, engine)

        then: "The result is a new object instance (Immutability)"
        result != null
        !initial.is(result) 
    }
}
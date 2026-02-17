package com.warhammer.service

import com.warhammer.service.ProbabilityEngine.RerollType
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class ProbabilityEngineSpec extends Specification {

    @Subject
    ProbabilityEngine engine = new ProbabilityEngine()

    @Unroll
    def "calculateSuccessProb for target #target+ (crit #crit+) with #reroll is #expected"() {
        expect:
        Math.abs(engine.calculateSuccessProb(target, reroll, crit) - expected) < 0.0001

        where:
        target | crit | reroll             | expected
        4      | 6    | RerollType.NONE    | 0.5          // 3/6
        
        // Reroll 1s on a 4+: 0.5 + (1/6 * 0.5)
        4      | 6    | RerollType.ONES    | 0.5833
        
        // Reroll Failed on a 3+: 0.666 + (0.333 * 0.666)
        3      | 6    | RerollType.FAILED  | 0.8888
        
        // FISHING: Reroll ALL on a 3+ (Targeting 6s):
        // P(6) + P(1-5) * P(3+) = 1/6 + (5/6 * 4/6) = 26/36
        3      | 6    | RerollType.ALL     | 0.7222
        
        // FISHING: Reroll ALL on a 2+ (Targeting 5s):
        // P(5+) + P(1-4) * P(2+) = 2/6 + (4/6 * 5/6) = 32/36
        2      | 5    | RerollType.ALL     | 0.8888
    }

    @Unroll
    def "calculateCritProb accurately handles fishing for threshold #threshold"() {
        expect:
        Math.abs(engine.calculateCritProb(threshold, reroll) - expected) < 0.0001

        where:
        threshold | reroll            | expected
        6         | RerollType.NONE   | 0.1666  // 1/6
        6         | RerollType.ALL    | 0.3055  // 1/6 + (5/6 * 1/6)
        5         | RerollType.FAILED | 0.5555  // 2/6 + (4/6 * 2/6)
    }
}
package com.warhammer.util

import spock.lang.Specification
import spock.lang.Unroll

class DamageProcessorSpec extends Specification {

    @Unroll
    def "Max damage for #desc should be #expectedMax"() {
        given: "A guaranteed wound count"
        double[] woundDist = new double[wounds + 1]
        woundDist[wounds] = 1.0

        when:
        double[] damageDist = DamageProcessor.calculateDamageDistribution(woundDist, expr)

        then:
        damageDist.length - 1 == expectedMax
        Math.abs(damageDist.sum() - 1.0) < 0.000001

        where:
        expr   | wounds | expectedMax | desc
        "3"    | 4      | 12          | "Flat 3 * 4 wounds"
        "D3"   | 2      | 6           | "2 * Max D3"
        "D6+2" | 1      | 8           | "1 * Max (6+2)"
        "D6-1" | 2      | 10          | "2 * Max (6-1)"
    }

    def "Probabilistic wounds should result in a complex damage weighted average"() {
        given: "A 50/50 chance of 1 wound or 2 wounds"
        double[] woundDist = [0, 0.5, 0.5] 

        when: "Damage is a flat 2"
        double[] dist = DamageProcessor.calculateDamageDistribution(woundDist, "2")

        then: "Result should be 50% chance of 2 damage, 50% chance of 4 damage"
        dist[2] == 0.5
        dist[4] == 0.5
        dist[0] == 0
    }

    def "Negative modifiers should siphon damage into the 0 index"() {
        given: "1 wound with D3-2 damage"
        double[] woundDist = [0, 1.0]

        when: "D3 rolls: 1-2=-1 (mapped to 0), 2-2=0, 3-2=1"
        double[] dist = DamageProcessor.calculateDamageDistribution(woundDist, "D3-2")

        then: "2/3 of results (the -1 and 0) should end up in index 0"
        Math.abs(dist[0] - 0.666666) < 0.0001
        Math.abs(dist[1] - 0.333333) < 0.0001
    }

    def "Dice parsing should be case-insensitive and handle whitespace"() {
        given: "1 wound"
        double[] woundDist = [0, 1.0]

        expect: "Various string formats result in same distribution"
        DamageProcessor.calculateDamageDistribution(woundDist, "d3 + 1")[2] == 0.3333333333333333
        DamageProcessor.calculateDamageDistribution(woundDist, "D6 - 2")[4] == 0.16666666666666666
    }

    def "Large scale convolution should maintain statistical integrity"() {
        given: "10 wounds (100% chance)"
        double[] woundDist = new double[11]
        woundDist[10] = 1.0

        when: "Damage is D6"
        double[] dist = DamageProcessor.calculateDamageDistribution(woundDist, "D6")

        then: "Total prob must be 1.0 and max damage is 60"
        Math.abs(dist.sum() - 1.0) < 0.000001
        dist.length == 61
        
        and: "The average damage should be 35.0 (10 * 3.5)"
        double actualAvg = 0
        dist.eachWithIndex { prob, i -> actualAvg += (i * prob) }
        Math.abs(actualAvg - 35.0) < 0.0001
    }
}
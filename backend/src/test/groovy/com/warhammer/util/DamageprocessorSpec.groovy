package com.warhammer.util

import spock.lang.Specification
import spock.lang.Unroll

class DamageProcessorSpec extends Specification {

    @Unroll
    def "calculateDamageDistribution for #desc should have expected max damage #expectedMax"() {
        given: "A wound distribution of 2 wounds (100% chance)"
        double[] woundDist = [0, 0, 1.0] 

        when: "Calculating damage for expression: #expr"
        double[] damageDist = DamageProcessor.calculateDamageDistribution(woundDist, expr)

        then: "The maximum damage index is correct"
        damageDist.length - 1 == expectedMax
        Math.abs(damageDist.sum() - 1.0) < 0.000001

        where:
        expr   | expectedMax | desc
        "2"    | 4           | "Flat 2 damage (2 * 2)"
        "D3"   | 6           | "D3 damage (2 * 3)"
        "D6+1" | 14          | "D6+1 damage (2 * 7)"
    }

    def "D3 damage should produce a flat 1/3 distribution for a single wound"() {
        given: "Exactly 1 wound"
        double[] woundDist = [0, 1.0]

        when:
        double[] dist = DamageProcessor.calculateDamageDistribution(woundDist, "D3")

        then: "Indices 1, 2, and 3 should each have ~33.3% probability"
        Math.abs(dist[1] - 0.333333) < 0.0001
        Math.abs(dist[2] - 0.333333) < 0.0001
        Math.abs(dist[3] - 0.333333) < 0.0001
        dist[0] == 0
    }

    def "Damage should handle empty or null expressions as 1 damage"() {
        given:
        double[] woundDist = [0, 1.0] as double[]

        expect:
        DamageProcessor.calculateDamageDistribution(woundDist, null)[1] == 1.0
        DamageProcessor.calculateDamageDistribution(woundDist, "")[1] == 1.0
    }
}
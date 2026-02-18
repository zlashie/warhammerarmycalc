package com.warhammer.util

import spock.lang.Specification
import spock.lang.Unroll

class ProbabilityMathSpec extends Specification {

    @Unroll
    def "combinations(n: #n, k: #k) should return #expected"() {
        expect:
        ProbabilityMath.combinations(n, k) == expected

        where:
        n  | k  || expected
        6  | 0  || 1       // Base case
        6  | 1  || 6       // nC1
        6  | 3  || 20      // Middle
        6  | 6  || 1       // nCn
        10 | 2  || 45      // Larger set
    }

    def "calculateBinomial should produce a distribution that sums to 1.0"() {
        given: "10 attacks at BS 4+ (p=0.5)"
        int n = 10
        double p = 0.5

        when:
        double[] dist = ProbabilityMath.calculateBinomial(n, p)

        then: "The sum of all probabilities must be 1"
        Math.abs(dist.sum() - 1.0) < 0.000001
        dist.length == n + 1
    }

    def "convolve should correctly combine two simple distributions"() {
        given: "Two coins (50/50 chance of 1 hit)"
        double[] coinA = [0.5, 0.5] // 0 hits or 1 hit
        double[] coinB = [0.5, 0.5]

        when: "We convolve (flip both)"
        double[] result = ProbabilityMath.convolve(coinA, coinB)

        then: "Outcome should be 0, 1, or 2 hits with 25/50/25 distribution"
        result.length == 3
        result[0] == 0.25 // 0.5 * 0.5
        result[1] == 0.50 // (0.5*0.5) + (0.5*0.5)
        result[2] == 0.25 // 0.5 * 0.5
    }
}
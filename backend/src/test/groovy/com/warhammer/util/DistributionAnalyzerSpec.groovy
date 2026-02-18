package com.warhammer.util

import com.warhammer.dto.CalculationResultDTO
import spock.lang.Specification

class DistributionAnalyzerSpec extends Specification {

    def "enrich should correctly calculate statistics for a guaranteed outcome"() {
        given: "A distribution where there is 100% chance of exactly 5 hits"
        double[] dist = [0, 0, 0, 0, 0, 1.0]
        def dto = new CalculationResultDTO([], 5)

        when: "We enrich the DTO"
        DistributionAnalyzer.enrich(dto, dist)

        then: "The mean and probability should be exact"
        dto.avgValue == 5.0
        dto.avgProb == 100.0
        dto.probAtLeastAvg == 100.0

        and: "The ranges should collapse to a single point"
        dto.range80 == "5 - 5"
        dto.rangeTop5 == "5 - 5"
        dto.rangeStd == "5 - 5"
    }

    def "enrich should calculate correct ranges for a simple 50/50 distribution"() {
        given: "A 50/50 chance of 0 or 1 hit (like a single BS 4+ shot)"
        double[] dist = [0.5, 0.5]
        def dto = new CalculationResultDTO([], 1)

        when: "We enrich the DTO"
        DistributionAnalyzer.enrich(dto, dist)

        then: "The average value should be 0.5"
        dto.avgValue == 0.5
        
        and: "P10 should be 0 and P90 should be 1"
        dto.range80 == "0 - 1"
        
        and: "Probability of at least average (0.5) is just the chance of 1 hit"
        dto.probAtLeastAvg == 50.0
    }

    def "findAbsoluteMax should ignore negligible probabilities"() {
        given: "A distribution with a tiny tail"
        // 0 hits: 20%, 1 hit: 80%, 2 hits: 0.0000001% (below significance cutoff)
        double[] dist = [0.2, 0.8, 0.00000001]
        def dto = new CalculationResultDTO([], 2)

        when:
        DistributionAnalyzer.enrich(dto, dist)

        then: "The top 5% range should ignore the negligible 2 hits"
        dto.rangeTop5.endsWith("- 1")
    }
}
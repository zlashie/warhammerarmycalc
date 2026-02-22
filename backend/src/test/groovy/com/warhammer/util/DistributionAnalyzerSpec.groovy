package com.warhammer.util

import com.warhammer.dto.CalculationResultDTO
import spock.lang.Specification
import spock.lang.Unroll

class DistributionAnalyzerSpec extends Specification {

    def "enrichHits should map stats to Hit fields only"() {
        given: "A simple distribution"
        double[] dist = [0.1, 0.9] 
        def dto = new CalculationResultDTO([], 1)

        when:
        DistributionAnalyzer.enrichHits(dto, dist)

        then: "Hit fields are populated"
        dto.avgValue == 0.9
        
        and: "Wound and Damage fields remain empty/default"
        dto.woundAvgValue == 0.0
        dto.damageAvgValue == 0.0
    }

    def "enrichDamage should map stats to Damage fields only"() {
        given: "A simple distribution"
        double[] dist = [0.1, 0.9]
        def dto = new CalculationResultDTO([], 1)

        when:
        DistributionAnalyzer.enrichDamage(dto, dist)

        then: "Damage fields are populated"
        dto.damageAvgValue == 0.9
        dto.damageRange80 == "0 - 1"
        
        and: "Hit fields remain empty"
        dto.avgValue == 0.0
    } 

    def "Percentile logic should correctly identify thresholds in skewed distributions"() {
        given: "A heavily skewed distribution"
        double[] dist = [0.05, 0.10, 0.70, 0.15]
        def dto = new CalculationResultDTO([], 3)

        when:
        DistributionAnalyzer.enrichHits(dto, dist)

        then: "P10 starts at index 1 (cumulative 0.15) and P90 ends at index 3 (cumulative 1.0)"
        dto.range80 == "1 - 3"
        
        and: "Top 5% (P95 to Absolute Max) is just index 3"
        dto.rangeTop5 == "3 - 3"
    }

    def "Standard Deviation range should reflect spread"() {
        given: "A spread-out distribution (Mean 1.0)"
        double[] dist = [0.5, 0, 0.5]
        def dto = new CalculationResultDTO([], 2)

        when:
        DistributionAnalyzer.enrichHits(dto, dist)

        then: "Mean is 1.0, StdDev is 1.0. Range is (1-1) to (1+1)"
        dto.rangeStd == "0 - 2"
    }

    def "Analyzer should handle edge-case empty inputs gracefully"() {
        given: "An empty array"
        double[] dist = []
        def dto = new CalculationResultDTO([], 0)

        when:
        DistributionAnalyzer.enrichHits(dto, dist)

        then: "Stats return safe zeros and ranges"
        dto.avgValue == 0.0
        dto.range80 == "0 - 0"
    }

    def "probAtLeastAvg should correctly sum tail probabilities for fractional averages"() {
        given: "A distribution where average is 1.5"
        double[] dist = [0.1, 0.4, 0.5] 
        def dto = new CalculationResultDTO([], 2)

        when:
        DistributionAnalyzer.enrichHits(dto, dist)

        then: "Average is 1.4. Prob of at least 1.4 includes index 2 only"
        dto.probAtLeastAvg == 50.0
    }
}
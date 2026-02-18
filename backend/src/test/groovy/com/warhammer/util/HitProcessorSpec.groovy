package com.warhammer.util

import com.warhammer.dto.CalculationRequestDTO
import spock.lang.Specification
import spock.lang.Unroll

class HitProcessorSpec extends Specification {

    @Unroll
    def "calculateUnitDistribution for BS #bs should have max index #expectedMaxIndex"() {
        given: "A request for a unit with specific stats"
        def request = new CalculationRequestDTO()
        request.numberOfModels = models
        request.attacksPerModel = attacks
        request.bsValue = bs

        when: "The distribution is calculated"
        double[] dist = HitProcessor.calculateUnitDistribution(request)

        then: "The array size should match total possible hits + 1"
        dist.length == expectedMaxIndex + 1
        
        and: "The sum of probabilities should be 1.0"
        Math.abs(dist.sum() - 1.0) < 0.000001

        where:
        models | attacks | bs || expectedMaxIndex
        10     | 1       | 4  || 10               // Standard infantry
        1      | 20      | 3  || 20               // Big vehicle
        5      | 2       | 2  || 10               // Elite unit
    }

    @Unroll
    def "probability of at least 1 hit for 1 attack at BS #bs should be #expectedProb"() {
        given: "A single shot request"
        def request = new CalculationRequestDTO(
            numberOfModels: 1,
            attacksPerModel: 1,
            bsValue: bs
        )

        when:
        double[] dist = HitProcessor.calculateUnitDistribution(request)
        // dist[0] is probability of 0 hits, dist[1] is probability of 1 hit
        double hitProb = dist[1]

        then: "The probability should match the D6 mechanics"
        Math.abs(hitProb - expectedProb) < 0.000001

        where:
        bs || expectedProb
        2  || 5/6          // 2,3,4,5,6 hit
        3  || 4/6          // 3,4,5,6 hit
        4  || 3/6          // 4,5,6 hit
        5  || 2/6          // 5,6 hit
        6  || 1/6          // 6 hits
    }
}
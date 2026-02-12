package com.warhammer.service

import com.warhammer.dto.CalculationRequest
import spock.lang.Specification

class CalculatorServiceSpec extends Specification {

    def "should increment the input value by one"() {
        given: "a calculator service and a request"
        def service = new CalculatorService()
        def request = new CalculationRequest(5)

        when: "the increment logic is called"
        def result = service.incrementValue(request)

        then: "the result should be exactly one higher"
        result == 6
    }

    def "should correctly increment various inputs"() {
        given: "the service"
        def service = new CalculatorService()

        expect: "the math to be correct for multiple scenarios"
        service.incrementValue(new CalculationRequest(input)) == expected

        where: "the data table is defined"
        input | expected
        1     | 2
        10    | 11
        99    | 100
    }
}
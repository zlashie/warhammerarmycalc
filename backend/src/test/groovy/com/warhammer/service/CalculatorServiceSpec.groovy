package com.warhammer.service

import com.warhammer.dto.*
import spock.lang.Specification
import spock.lang.Subject

class CalculatorServiceSpec extends Specification {

    @Subject
    def service = new CalculatorService()

    def "should calculate basic expected hits for a unit"() {
        given: "A nested DTO structure matching the new Java records"
        def stats = new UnitStatsDTO("5", "2", "3+", "4", "-1", "1")
        def toggles = new UnitTogglesDTO(false, null, null, null, null, null, false)
        def unit = new UnitDTO(1, "Intercessors", "100", stats, toggles)
        
        def request = new CalculationRequestDTO([unit])

        when: "the service calculates the army"
        def result = service.calculateArmy(request)

        then: "the result reflects models * attacks (5 * 2 = 10)"
        result.expectedValue() == 10.0
    }

    def "should handle malformed string stats like '3+' gracefully"() {
        given: "Stats with Warhammer notation"
        def stats = new UnitStatsDTO("1", "D6", "4+", "4", "0", "1")
        def emptyToggles = new UnitTogglesDTO(false, null, null, null, null, null, false)
        
        def request = new CalculationRequestDTO([
            new UnitDTO(1, "Test", "0", stats, emptyToggles)
        ])

        when:
        def result = service.calculateArmy(request)

        then: "The parser extracts the numeric portion of the string"
        result.expectedValue() == 6.0
    }
}
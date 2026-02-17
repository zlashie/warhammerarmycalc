package com.warhammer.service

import com.warhammer.dto.*
import spock.lang.Specification
import spock.lang.Subject

class CalculatorServiceSpec extends Specification {

    @Subject
    def service = new CalculatorService()
}
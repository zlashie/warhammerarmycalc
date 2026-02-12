package com.warhammer.controller;

import com.warhammer.dto.CalculationRequest;
import com.warhammer.service.CalculatorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calc")
@CrossOrigin(origins = "http://localhost:4200")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @PostMapping("/increment")
    public int getIncrement(@RequestBody CalculationRequest request) {
        return calculatorService.incrementValue(request);
    }
}
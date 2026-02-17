package com.warhammer.controller;

import com.warhammer.dto.CalculationRequestDTO;
import com.warhammer.dto.CalculationResultDTO; 
import com.warhammer.service.CalculatorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calculator")
@CrossOrigin(origins = "http://localhost:4200")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @PostMapping("/calculate")
    public CalculationResultDTO calculateArmy(@RequestBody CalculationRequestDTO request) {
        return calculatorService.calculateArmy(request);
    }
}
package com.warhammer.controller;

import com.warhammer.dto.CalculationRequestDTO;
import com.warhammer.dto.CalculationResultDTO;
import com.warhammer.service.CalculatorService;
import org.springframework.web.bind.annotation.*;
import java.util.List; 

@RestController
@RequestMapping("/api/calculate")
@CrossOrigin(origins = "*") 
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @PostMapping
    public CalculationResultDTO calculate(@RequestBody List<CalculationRequestDTO> requests) {
        return calculatorService.calculateArmyHits(requests);
    }
}
package com.warhammer.service;

import org.springframework.stereotype.Service;
import com.warhammer.dto.CalculationRequest;

@Service
public class CalculatorService {

    public int incrementValue(CalculationRequest request) {
        return request.inputValue() + 1;
    }
}

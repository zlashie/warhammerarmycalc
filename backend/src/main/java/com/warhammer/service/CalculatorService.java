package com.warhammer.service;

import com.warhammer.dto.CalculationRequestDTO;
import com.warhammer.dto.CalculationResultDTO;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CalculatorService {

    public CalculationResultDTO calculateArmyHits(List<CalculationRequestDTO> requests) {
        double totalExpectedValue = 0;
        double totalVariance = 0;
        int totalMaxPossible = 0;

        for (CalculationRequestDTO request : requests) {
            int n = request.getNumberOfModels() * request.getAttacksPerModel();
            double p = (7.0 - request.getBsValue()) / 6.0;

            totalExpectedValue += (n * p);
            
            totalVariance += (n * p * (1 - p));
            
            totalMaxPossible += n;
        }

        double totalStdDev = Math.sqrt(totalVariance);

        return new CalculationResultDTO(
            "Army Total",
            round(totalExpectedValue),
            round(totalStdDev),
            totalMaxPossible 
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
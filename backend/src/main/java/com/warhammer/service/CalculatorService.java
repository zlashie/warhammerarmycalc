package com.warhammer.service;

import com.warhammer.dto.CalculationRequestDTO;
import com.warhammer.dto.CalculationResultDTO;
import com.warhammer.dto.UnitDTO; 
import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

    public CalculationResultDTO calculateArmy(CalculationRequestDTO request) {
        if (request.units() == null || request.units().isEmpty()) {
            return new CalculationResultDTO(0.0, 0.0);
        }

        double totalExpectedDamage = request.units().stream()
            .mapToDouble(this::calculateUnitExpectedDamage)
            .sum();

        return new CalculationResultDTO(totalExpectedDamage, 0.0);
    }

    private double calculateUnitExpectedDamage(UnitDTO unit) {
        double m = parseStat(unit.stats().models());
        double a = parseStat(unit.stats().attacks());
        return m * a;
    }

    private double parseStat(String stat) {
        if (stat == null || stat.isEmpty()) return 0;
        String clean = stat.replaceAll("[^\\d.]", ""); 
        try {
            return clean.isEmpty() ? 0 : Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
package com.warhammer.util;

import com.warhammer.dto.CalculationRequestDTO;

public class HitProcessor {

    public static double[] calculateUnitDistribution(CalculationRequestDTO request) {
        int n = request.getNumberOfModels() * request.getAttacksPerModel();

        double p = (7.0 - request.getBsValue()) / 6.0;

        return ProbabilityMath.calculateBinomial(n, p);
    }
}
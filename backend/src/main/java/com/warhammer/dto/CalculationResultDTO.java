package com.warhammer.dto;

public record CalculationResultDTO(
    double expectedValue,
    double standardDeviation
) {}
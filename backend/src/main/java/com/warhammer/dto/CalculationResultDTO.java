package com.warhammer.dto;

public class CalculationResultDTO {
    private String unitName;
    private double expectedValue;
    private double standardDeviation;
    private int maxPossibleValue;

    public CalculationResultDTO(String unitName, double expectedValue, double standardDeviation, int maxPossibleValue) {
        this.unitName = unitName;
        this.expectedValue = expectedValue;
        this.standardDeviation = standardDeviation;
        this.maxPossibleValue = maxPossibleValue;
    }

    public String getUnitName() { return unitName; }
    public double getExpectedValue() { return expectedValue; }
    public double getStandardDeviation() { return standardDeviation; }
    public int getMaxPossibleValue() { return maxPossibleValue; }
}
package com.warhammer.dto;

import java.util.List;

public class CalculationResultDTO {
    private List<Double> probabilities; 
    private int maxHits;

    public CalculationResultDTO(List<Double> probabilities, int maxHits) {
        this.probabilities = probabilities;
        this.maxHits = maxHits;
    }

    public List<Double> getProbabilities() { return probabilities; }
    public int getMaxHits() { return maxHits; }
}
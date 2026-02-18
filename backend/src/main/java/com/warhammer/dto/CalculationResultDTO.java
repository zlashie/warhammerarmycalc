package com.warhammer.dto;

import java.util.List;

public class CalculationResultDTO {
    private List<Double> probabilities; 
    private int maxHits;
    
    // Statistical Fields
    private double avgValue;
    private double avgProb;
    private String range80;     
    private String rangeStd;
    private double probAtLeastAvg; 
    private String rangeTop5;

    public CalculationResultDTO(List<Double> probabilities, int maxHits) {
        this.probabilities = probabilities;
        this.maxHits = maxHits;
    }

    // Getters and Setters
    public List<Double> getProbabilities() { return probabilities; }
    public int getMaxHits() { return maxHits; }
    public double getAvgValue() { return avgValue; }
    public void setAvgValue(double avgValue) { this.avgValue = avgValue; }
    public double getAvgProb() { return avgProb; }
    public void setAvgProb(double avgProb) { this.avgProb = avgProb; }
    public String getRange80() { return range80; }
    public void setRange80(String range80) { this.range80 = range80; }
    public String getRangeStd() { return rangeStd; }
    public void setRangeStd(String rangeStd) { this.rangeStd = rangeStd; }
    public double getProbAtLeastAvg() { return probAtLeastAvg; }
    public void setProbAtLeastAvg(double probAtLeastAvg) { this.probAtLeastAvg = probAtLeastAvg; }
    public String getRangeTop5() { return rangeTop5; }
    public void setRangeTop5(String rangeTop5) { this.rangeTop5 = rangeTop5; }
}
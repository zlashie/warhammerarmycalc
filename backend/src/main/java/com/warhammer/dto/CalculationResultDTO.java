package com.warhammer.dto;

import java.util.List;

public class CalculationResultDTO {
    // --- Hit Fields ---
    private List<Double> probabilities; 
    private int maxHits;
    private double avgValue;
    private double avgProb;
    private String range80;     
    private String rangeStd;
    private double probAtLeastAvg; 
    private String rangeTop5;

    // --- NEW: Wound Fields ---
    private List<Double> woundProbabilities;
    private double woundAvgValue;
    private String woundRange80;
    private String woundRangeTop5;
    private double woundProbAtLeastAvg;

    public CalculationResultDTO(List<Double> probabilities, int maxHits) {
        this.probabilities = probabilities;
        this.maxHits = maxHits;
    }

    // --- Hit Getters and Setters ---
    public List<Double> getProbabilities() { return probabilities; }
    public void setProbabilities(List<Double> probabilities) { this.probabilities = probabilities; }
    public int getMaxHits() { return maxHits; }
    public void setMaxHits(int maxHits) { this.maxHits = maxHits; }
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

    // --- NEW: Wound Getters and Setters ---
    public List<Double> getWoundProbabilities() { return woundProbabilities; }
    public void setWoundProbabilities(List<Double> woundProbabilities) { this.woundProbabilities = woundProbabilities; }
    public double getWoundAvgValue() { return woundAvgValue; }
    public void setWoundAvgValue(double woundAvgValue) { this.woundAvgValue = woundAvgValue; }
    public String getWoundRange80() { return woundRange80; }
    public void setWoundRange80(String woundRange80) { this.woundRange80 = woundRange80; }
    public String getWoundRangeTop5() { return woundRangeTop5; }
    public void setWoundRangeTop5(String woundRangeTop5) { this.woundRangeTop5 = woundRangeTop5; }
    public double getWoundProbAtLeastAvg() { return woundProbAtLeastAvg; }
    public void setWoundProbAtLeastAvg(double woundProbAtLeastAvg) { this.woundProbAtLeastAvg = woundProbAtLeastAvg; }
}
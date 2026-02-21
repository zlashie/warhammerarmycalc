package com.warhammer.dto;

import java.util.List;
import java.util.ArrayList;

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

    // --- Wound Fields ---
    private List<Double> woundProbabilities;
    private double woundAvgValue;
    private String woundRange80;
    private String woundRangeTop5;
    private double woundProbAtLeastAvg;

    // --- Damage Fields ---
    private List<Double> damageProbabilities;
    private double damageAvgValue;
    private String damageRange80;
    private String damageRangeTop5;
    private double damageProbAtLeastAvg;

    // --- Toughness Analysis Fields ---
    private List<ToughnessNode> toughnessScaling = new ArrayList<>();

    // --- Damage Analysis Fields ---
    private List<SaveNode> saveScaling = new ArrayList<>();

    public CalculationResultDTO(List<Double> probabilities, int maxHits) {
        this.probabilities = probabilities;
        this.maxHits = maxHits;
    }

    // --- Graph Data Structure ---
    public static class ToughnessNode {
        public int toughness;
        public double average;
        public double lower80; 
        public double upper80; 

        public ToughnessNode(int t, double avg, double low, double high) {
            this.toughness = t;
            this.average = avg;
            this.lower80 = low;
            this.upper80 = high;
        }
    }

    public static class SaveNode {
        public String saveLabel; 
        public double average;
        public double lower80;
        public double upper80;

        public SaveNode(String label, double avg, double low, double high) {
            this.saveLabel = label;
            this.average = avg;
            this.lower80 = low;
            this.upper80 = high;
        }
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

    // --- Wound Getters and Setters ---
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

    // --- Damage Getters and Setters ---
    public List<Double> getDamageProbabilities() { return damageProbabilities; } 
    public void setDamageProbabilities(List<Double> damageProbabilities) { this.damageProbabilities = damageProbabilities; }
    public double getDamageAvgValue() { return damageAvgValue; }
    public void setDamageAvgValue(double damageAvgValue) { this.damageAvgValue = damageAvgValue; }
    public String getDamageRange80() { return damageRange80; }
    public void setDamageRange80(String damageRange80) { this.damageRange80 = damageRange80; }
    public String getDamageRangeTop5() { return damageRangeTop5; }
    public void setDamageRangeTop5(String damageRangeTop5) { this.damageRangeTop5 = damageRangeTop5; }
    public double getDamageProbAtLeastAvg() { return damageProbAtLeastAvg; }
    public void setDamageProbAtLeastAvg(double damageProbAtLeastAvg) { this.damageProbAtLeastAvg = damageProbAtLeastAvg; }

    // --- Toughness Analysis Getters and Setters ---
    public List<ToughnessNode> getToughnessScaling() { return toughnessScaling; }
    public void setToughnessScaling(List<ToughnessNode> scaling) { this.toughnessScaling = scaling; }

    // --- Damage Analysis Getters and Setters ---
    public List<SaveNode> getSaveScaling() { return saveScaling; }
    public void setSaveScaling(List<SaveNode> saveScaling) { this.saveScaling = saveScaling; }
}
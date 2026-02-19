package com.warhammer.dto;

public class CalculationRequestDTO {
    private String unitName;
    private int numberOfModels;
    private int attacksPerModel;
    private int bsValue;
    private boolean sustainedHits;
    private String sustainedValue;
    private String rerollType;

    public CalculationRequestDTO() {}

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public int getNumberOfModels() { return numberOfModels; }
    public void setNumberOfModels(int numberOfModels) { this.numberOfModels = numberOfModels; }

    public int getAttacksPerModel() { return attacksPerModel; }
    public void setAttacksPerModel(int attacksPerModel) { this.attacksPerModel = attacksPerModel; }

    public int getBsValue() { return bsValue; }
    public void setBsValue(int bsValue) { this.bsValue = bsValue; }

    public boolean isSustainedHits() { return sustainedHits; }
    public void setSustainedHits(boolean sustainedHits) { this.sustainedHits = sustainedHits; }

    public String getSustainedValue() { return sustainedValue; }
    public void setSustainedValue(String sustainedValue) { this.sustainedValue = sustainedValue; }

    public String getRerollType() { return rerollType == null ? "NONE" : rerollType; }
    public void setRerollType(String rerollType) { this.rerollType = rerollType; }
}
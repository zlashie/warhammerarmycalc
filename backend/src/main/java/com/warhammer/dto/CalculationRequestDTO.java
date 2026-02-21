package com.warhammer.dto;

public class CalculationRequestDTO {
    private String unitName;
    private int numberOfModels;
    private int attacksPerModel;
    private int bsValue;
    private int strength = 4;
    private int ap;
    private boolean sustainedHits;
    private String sustainedValue;
    private String rerollType;
    private int critHitValue = 6;
    private String damageValue;
    private boolean lethalHits;
    private String woundRerollType; 
    private int critWoundValue = 6;
    private boolean devastatingWounds;
    private boolean plusOneToHit;
    private boolean plusOneToWound;

    public CalculationRequestDTO() {}

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public int getNumberOfModels() { return numberOfModels; }
    public void setNumberOfModels(int numberOfModels) { this.numberOfModels = numberOfModels; }

    public int getAttacksPerModel() { return attacksPerModel; }
    public void setAttacksPerModel(int attacksPerModel) { this.attacksPerModel = attacksPerModel; }

    public int getBsValue() { return bsValue; }
    public void setBsValue(int bsValue) { this.bsValue = bsValue; }

    public int getStrength() { return strength > 0 ? strength : 4; }
    public void setStrength(int strength) { this.strength = strength; }

    public int getAp() { return ap; }
    public void setAp(int ap) { this.ap = ap; }

    public boolean isSustainedHits() { return sustainedHits; }
    public void setSustainedHits(boolean sustainedHits) { this.sustainedHits = sustainedHits; }

    public String getSustainedValue() { return sustainedValue; }
    public void setSustainedValue(String sustainedValue) { this.sustainedValue = sustainedValue; }

    public String getRerollType() { return rerollType == null ? "NONE" : rerollType; }
    public void setRerollType(String rerollType) { this.rerollType = rerollType; }

    public int getCritHitValue() { return critHitValue > 0 ? critHitValue : 6; }
    public void setCritHitValue(int critHitValue) { this.critHitValue = critHitValue; }

    public String getDamageValue() { return (damageValue == null || damageValue.isBlank()) ? "1" : damageValue; }
    public void setDamageValue(String damageValue) { this.damageValue = damageValue; }

    public boolean isLethalHits() { return lethalHits; }
    public void setLethalHits(boolean lethalHits) { this.lethalHits = lethalHits; }

    public String getWoundRerollType() { return woundRerollType == null ? "NONE" : woundRerollType; }
    public void setWoundRerollType(String woundRerollType) { this.woundRerollType = woundRerollType; }

    public int getCritWoundValue() { return critWoundValue > 0 ? critWoundValue : 6; }
    public void setCritWoundValue(int critWoundValue) { this.critWoundValue = critWoundValue; }

    public boolean isDevastatingWounds() { return devastatingWounds; }
    public void setDevastatingWounds(boolean devastatingWounds) { this.devastatingWounds = devastatingWounds; }

    public boolean isPlusOneToHit() { return plusOneToHit; }
    public void setPlusOneToHit(boolean plusOneToHit) { this.plusOneToHit = plusOneToHit; }
    
    public boolean isPlusOneToWound() { return plusOneToWound; }
    public void setPlusOneToWound(boolean plusOneToWound) { this.plusOneToWound = plusOneToWound; }
}
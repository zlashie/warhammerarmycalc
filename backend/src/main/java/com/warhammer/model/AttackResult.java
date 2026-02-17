package com.warhammer.model;

import com.warhammer.service.ProbabilityEngine.RerollType;

/**
 * An immutable snapshot of an attack sequence's current statistical state.
 * Tracks both the mean and variance to allow for Standard Deviation calculation.
 */
public record AttackResult(
    double attacksMean,
    double attacksVariance,
    double hitPool,        
    double autoWounds,    
    double damageMean,
    double damageVariance,
    int critThreshold,     
    RerollType hitReroll,
    int critWoundThreshold, 
    RerollType woundReroll,
    double finalWounds
) {
    public AttackResult transitionToAutoWounds(double amount) {
        return new AttackResult(
            attacksMean, attacksVariance, hitPool - amount, autoWounds + amount, 
            damageMean, damageVariance, 
            critThreshold, hitReroll, 
            critWoundThreshold, woundReroll, 
            finalWounds
        );
    }

    public AttackResult addExtraHits(double amount) {
        return new AttackResult(
            attacksMean, attacksVariance, hitPool + amount, 
            autoWounds, damageMean, damageVariance, 
            critThreshold, hitReroll, 
            critWoundThreshold, woundReroll, 
            finalWounds
        );
    }

    public AttackResult withFinalWounds(double totalWounds) {
        return new AttackResult(
            attacksMean, attacksVariance, hitPool, 
            autoWounds, damageMean, damageVariance, 
            critThreshold, hitReroll, 
            critWoundThreshold, woundReroll, 
            totalWounds
        );
    }

    public AttackResult withCritThreshold(int threshold) {
    return new AttackResult(
            attacksMean, attacksVariance, hitPool, 
            autoWounds, damageMean, damageVariance, 
            threshold, hitReroll, 
            critWoundThreshold, woundReroll, 
            finalWounds
        );
    }

    public AttackResult withHitReroll(RerollType reroll) {
    return new AttackResult(
            attacksMean, attacksVariance, hitPool, 
            autoWounds, damageMean, damageVariance, 
            critThreshold, reroll, 
            critWoundThreshold, woundReroll, 
            finalWounds
        );
    }

    public AttackResult withWoundReroll(RerollType reroll) {
        return new AttackResult(
            attacksMean, attacksVariance, hitPool, autoWounds, 
            damageMean, damageVariance, 
            critThreshold, hitReroll, 
            critWoundThreshold, reroll, 
            finalWounds);
    }

    public AttackResult withCritWoundThreshold(int threshold) {
        return new AttackResult(
            attacksMean, attacksVariance, hitPool, autoWounds, 
            damageMean, damageVariance, 
            critThreshold, hitReroll, 
            threshold, woundReroll, 
            finalWounds);
    }
}
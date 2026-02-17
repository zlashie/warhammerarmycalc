package com.warhammer.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.warhammer.dto.CalculationRequestDTO;
import com.warhammer.dto.CalculationResultDTO;
import com.warhammer.dto.UnitDTO;
import com.warhammer.dto.UnitTogglesDTO;
import com.warhammer.service.ProbabilityEngine.RerollType;
import com.warhammer.service.rules.AttackRule;
import com.warhammer.model.AttackResult;
import com.warhammer.util.DiceParser;

@Service
public class CalculatorService {
    private final ProbabilityEngine probabilityEngine;
    private final List<AttackRule> rules;
    public static final int DEFAULT_CRIT = 6;
    public static final int DEFAULT_WOUND = 4;
    public static final int ZERO = 0;

    public CalculatorService(ProbabilityEngine probabilityEngine, List<AttackRule> rules) {
        this.probabilityEngine = probabilityEngine;
        this.rules = rules;
    }

    public CalculationResultDTO calculateArmy(CalculationRequestDTO request) {
        if (request.units() == null || request.units().isEmpty()) {
            return new CalculationResultDTO(ZERO, ZERO);
        }

        double totalMeanDamage = request.units().stream()
            .map(this::calculateUnitDamage)
            .mapToDouble(res -> res.finalWounds() * res.damageMean())
            .sum();

        return new CalculationResultDTO(totalMeanDamage, ZERO);
    }

    private AttackResult calculateUnitDamage(UnitDTO unit) {
        DiceParser.DiceStat attackStat = DiceParser.parse(unit.stats().attacks());
        DiceParser.DiceStat damageStat = DiceParser.parse(unit.stats().damage());
        double models = DiceParser.parse(unit.stats().models()).mean();

    AttackResult res = new AttackResult(
        attackStat.mean() * models, attackStat.variance() * models,
        attackStat.mean() * models, ZERO,
        damageStat.mean(), damageStat.variance(),
        DEFAULT_CRIT, ProbabilityEngine.RerollType.NONE, 
        DEFAULT_CRIT, ProbabilityEngine.RerollType.NONE,
        ZERO
    );

        for (AttackRule rule : rules) {
            if (rule.isApplicable(unit.toggles())) {
                res = rule.apply(res, unit.toggles(), probabilityEngine);
            }
        }

        int bsTarget = parseTarget(unit.stats().bsWs());

        // --- HIT RESOLUTION ---
        double pCrit = probabilityEngine.calculateCritProb(res.critThreshold(), res.hitReroll());

        double pHitTotal;
        if (res.hitReroll() == RerollType.ALL) {
            double pCritInitial = probabilityEngine.calculateCritProb(res.critThreshold(), RerollType.NONE);
            double pHitInitial = probabilityEngine.calculateSuccessProb(bsTarget, RerollType.NONE);
            
            pHitTotal = pCritInitial + ((1.0 - pCritInitial) * pHitInitial);
        } else {
            pHitTotal = probabilityEngine.calculateSuccessProb(bsTarget, res.hitReroll());
        }

        double pNormalHit = Math.max(ZERO, pHitTotal - pCrit);

        // --- WOUND RESOLUTION ---
        double hitsRollingToWound = res.hitPool() * pNormalHit;

        double pCritWound = probabilityEngine.calculateCritProb(res.critWoundThreshold(), res.woundReroll());
        double pWoundTotal;

        if (res.woundReroll() == RerollType.ALL) {
            double pCritInitial = probabilityEngine.calculateCritProb(res.critWoundThreshold(), RerollType.NONE);
            double pWoundInitial = probabilityEngine.calculateSuccessProb(DEFAULT_WOUND, RerollType.NONE);
            pWoundTotal = pCritInitial + ((1.0 - pCritInitial) * pWoundInitial);
        } else {
            pWoundTotal = probabilityEngine.calculateSuccessProb(DEFAULT_WOUND, res.woundReroll());
        }

        double pNormalWound = Math.max(0, pWoundTotal - pCritWound);

        double normalWounds = hitsRollingToWound * pNormalWound;
        double criticalWounds = hitsRollingToWound * pCritWound;

        double devastatingWounds = applyDevastatingWounds(criticalWounds, unit.toggles());

        double totalWounds = res.autoWounds() + normalWounds + criticalWounds;
        return res.withFinalWounds(totalWounds);
    }

    private int parseTarget(String stat) {
        if (stat == null) return 7;
        String clean = stat.replaceAll("[^\\d]", "");
        return clean.isEmpty() ? 7 : Integer.parseInt(clean);
    }

    // Placeholder until implemented
    private double applyDevastatingWounds(double criticalWounds, UnitTogglesDTO toggles) {
    if (toggles.devastating()) {
        return criticalWounds;
    }
    return 0.0;
}
}
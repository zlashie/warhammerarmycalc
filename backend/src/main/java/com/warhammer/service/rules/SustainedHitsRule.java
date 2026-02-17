package com.warhammer.service.rules;

import com.warhammer.dto.UnitTogglesDTO;
import com.warhammer.model.AttackResult;
import com.warhammer.service.ProbabilityEngine;
import com.warhammer.util.DiceParser;
import com.warhammer.util.DiceParser.DiceStat;
import org.springframework.stereotype.Component;

@Component
public class SustainedHitsRule implements AttackRule {

    @Override
    public boolean isApplicable(UnitTogglesDTO toggles) {
        return toggles.sustained() != null && !toggles.sustained().toString().isBlank();
    }

    @Override
    public AttackResult apply(AttackResult res, UnitTogglesDTO toggles, ProbabilityEngine engine) {
        double pCrit = engine.calculateCritProb(res.critThreshold(), res.hitReroll());
        DiceStat sustainedBonus = DiceParser.parse(toggles.sustained().toString());
        double extraHitsGenerated = (res.attacksMean() * pCrit) * sustainedBonus.mean();
        
        return res.addExtraHits(extraHitsGenerated);
    }
}
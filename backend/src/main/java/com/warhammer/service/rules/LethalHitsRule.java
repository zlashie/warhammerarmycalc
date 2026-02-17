package com.warhammer.service.rules;

import com.warhammer.dto.UnitTogglesDTO;
import com.warhammer.model.AttackResult;
import com.warhammer.service.ProbabilityEngine;
import org.springframework.stereotype.Component;

@Component
public class LethalHitsRule implements AttackRule {
    @Override
    public boolean isApplicable(UnitTogglesDTO toggles) {
        return toggles.lethal();
    }

    @Override
    public AttackResult apply(AttackResult res, UnitTogglesDTO toggles, ProbabilityEngine engine) {
        double pCrit = engine.calculateCritProb(res.critThreshold(), res.hitReroll());
        double autoWoundsGenerated = res.attacksMean() * pCrit;
        return res.transitionToAutoWounds(autoWoundsGenerated);
    }
}
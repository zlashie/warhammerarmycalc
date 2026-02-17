package com.warhammer.service.rules;

import com.warhammer.dto.UnitTogglesDTO;
import com.warhammer.model.AttackResult;
import com.warhammer.service.ProbabilityEngine;
import com.warhammer.service.ProbabilityEngine.RerollType;
import org.springframework.stereotype.Component;

@Component
public class RerollHitsRule implements AttackRule {

    @Override
    public boolean isApplicable(UnitTogglesDTO toggles) {
        return toggles.rerollHits() != null;
    }

    @Override
    public AttackResult apply(AttackResult res, UnitTogglesDTO toggles, ProbabilityEngine engine) {
        String input = toggles.rerollHits().toString().toUpperCase();
        
        RerollType type = switch(input) {
            case "1S" -> RerollType.ONES;
            case "FAIL" -> RerollType.FAILED;
            case "ALL" -> RerollType.ALL;
            default -> RerollType.NONE;
        };

        return res.withHitReroll(type);
    }
}
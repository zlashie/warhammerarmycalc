package com.warhammer.service.rules;

import com.warhammer.service.ProbabilityEngine;
import org.springframework.stereotype.Component;
import com.warhammer.dto.UnitTogglesDTO;
import com.warhammer.model.AttackResult;

@Component
public class AntiXRule implements AttackRule {

    public static final int DEFAULT_CRIT = 6;

    @Override
    public boolean isApplicable(UnitTogglesDTO toggles) {
        return toggles.antiX() != null && !toggles.antiX().toString().isBlank();
    }

    @Override
    public AttackResult apply(AttackResult res, UnitTogglesDTO toggles, ProbabilityEngine engine) {
        String val = toggles.antiX().toString().replaceAll("[^\\d]", "");
        int threshold = val.isEmpty() ? DEFAULT_CRIT : Integer.parseInt(val);
        return res.withCritWoundThreshold(threshold);
    }
}

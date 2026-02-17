package com.warhammer.service.rules;

import com.warhammer.dto.UnitTogglesDTO;
import com.warhammer.model.AttackResult;
import com.warhammer.service.ProbabilityEngine;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

@Component
@Order(1) 
public class CritThresholdRule implements AttackRule {

    public static final int DEFAULT_CRIT = 6;

    @Override
    public boolean isApplicable(UnitTogglesDTO toggles) {
        return toggles.crit() != null && !toggles.crit().toString().isBlank();
    }

    @Override
    public AttackResult apply(AttackResult res, UnitTogglesDTO toggles, ProbabilityEngine engine) {
        String critValue = toggles.crit().toString().replaceAll("[^\\d]", "");
        int threshold = critValue.isEmpty() ? DEFAULT_CRIT : Integer.parseInt(critValue);

        return res.withCritThreshold(threshold);
    }
}
package com.warhammer.service.rules;

import com.warhammer.dto.UnitTogglesDTO;
import com.warhammer.model.AttackResult;
import com.warhammer.service.ProbabilityEngine;

public interface AttackRule {
    boolean isApplicable(UnitTogglesDTO toggles);
    AttackResult apply(AttackResult current, UnitTogglesDTO toggles, ProbabilityEngine engine);
}
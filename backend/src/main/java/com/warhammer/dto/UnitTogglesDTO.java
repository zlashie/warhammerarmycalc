package com.warhammer.dto;

public record UnitTogglesDTO(
    boolean lethal,
    Object sustained,
    Object crit,
    Object rerollHits,
    Object rerollWounds,
    Object antiX,
    boolean devastating
) {}
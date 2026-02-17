package com.warhammer.dto;

public record UnitDTO(
    int id,
    String name,
    String points,
    UnitStatsDTO stats,
    UnitTogglesDTO toggles
) {}
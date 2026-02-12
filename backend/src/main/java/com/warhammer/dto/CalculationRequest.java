package com.warhammer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CalculationRequest(
    @JsonProperty("inputValue") 
    int inputValue
) {}
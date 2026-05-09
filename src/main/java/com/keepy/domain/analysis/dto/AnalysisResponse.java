package com.keepy.domain.analysis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisResponse(
        String productName,
        String brand,
        String category,
        BigDecimal estimatedPrice
) {}

package com.keepy.domain.analysis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisResponse(
        String productName,
        String brand,
        String category,
        BigDecimal estimatedPrice,
        String screenshotUrl,
        List<ShoppingOptionResult> shoppingOptions
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ShoppingOptionResult(
            String siteName,
            String siteUrl,
            BigDecimal price,
            String deliveryFee
    ) {}
}

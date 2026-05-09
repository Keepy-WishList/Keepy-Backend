package com.keepy.domain.item.dto;

import com.keepy.domain.item.entity.ShoppingOption;

import java.math.BigDecimal;

public record ShoppingOptionDto(
        Long id,
        String siteName,
        String siteUrl,
        BigDecimal price,
        Integer deliveryDays,
        String deliveryFee
) {
    public static ShoppingOptionDto from(ShoppingOption option) {
        return new ShoppingOptionDto(
                option.getId(),
                option.getSiteName(),
                option.getSiteUrl(),
                option.getPrice(),
                option.getDeliveryDays(),
                option.getDeliveryFee()
        );
    }
}

package com.keepy.domain.item.dto;

import com.keepy.domain.item.entity.Category;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;

public record ItemSaveRequest(
        @NotBlank(message = "제품명을 입력해주세요.")
        String productName,

        String brand,

        Category category,

        BigDecimal price,

        String currency,

        String imageUrl,

        String screenshotUrl,

        String description,

        String memo,

        List<ShoppingOptionSaveRequest> shoppingOptions
) {
    public record ShoppingOptionSaveRequest(
            String siteName,
            String siteUrl,
            BigDecimal price,
            String currency,
            Integer deliveryDays,
            String deliveryFee
    ) {}
}

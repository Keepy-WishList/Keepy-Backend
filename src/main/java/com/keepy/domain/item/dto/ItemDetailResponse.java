package com.keepy.domain.item.dto;

import com.keepy.domain.item.entity.Item;
import com.keepy.domain.item.entity.ShoppingOption;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ItemDetailResponse(
        Long id,
        String productName,
        String brand,
        String category,
        BigDecimal price,
        String imageUrl,
        String screenshotUrl,
        String description,
        String memo,
        Boolean isPurchased,
        List<ShoppingOptionDto> shoppingOptions,
        LocalDateTime createdAt
) {
    public static ItemDetailResponse from(Item item, List<ShoppingOption> shoppingOptions) {
        return new ItemDetailResponse(
                item.getId(),
                item.getProductName(),
                item.getBrand(),
                item.getCategory() != null ? item.getCategory().name() : null,
                item.getPrice(),
                item.getImageUrl(),
                item.getScreenshotUrl(),
                item.getDescription(),
                item.getMemo(),
                item.getIsPurchased(),
                shoppingOptions.stream()
                        .map(ShoppingOptionDto::from)
                        .toList(),
                item.getCreatedAt()
        );
    }
}

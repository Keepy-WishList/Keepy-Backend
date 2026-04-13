package com.keepy.domain.item.dto;

import com.keepy.domain.item.entity.Item;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemListResponse(
        Long id,
        String productName,
        String brand,
        String category,
        BigDecimal price,
        String currency,
        String imageUrl,
        Boolean isPurchased,
        String bestSiteName,
        LocalDateTime createdAt
) {
    public static ItemListResponse from(Item item) {
        String bestSiteName = item.getShoppingOptions().isEmpty()
                ? null
                : item.getShoppingOptions().getFirst().getSiteName();

        return new ItemListResponse(
                item.getId(),
                item.getProductName(),
                item.getBrand(),
                item.getCategory() != null ? item.getCategory().name() : null,
                item.getPrice(),
                item.getCurrency(),
                item.getImageUrl(),
                item.getIsPurchased(),
                bestSiteName,
                item.getCreatedAt()
        );
    }
}

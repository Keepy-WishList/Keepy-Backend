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
        String imageUrl,
        Boolean isPurchased,
        LocalDateTime createdAt
) {
    public static ItemListResponse from(Item item) {
        return new ItemListResponse(
                item.getId(),
                item.getProductName(),
                item.getBrand(),
                item.getCategory() != null ? item.getCategory().name() : null,
                item.getPrice(),
                item.getImageUrl(),
                item.getIsPurchased(),
                item.getCreatedAt()
        );
    }
}

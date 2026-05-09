package com.keepy.domain.item.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String productName;

    private String brand;

    @Enumerated(EnumType.STRING)
    private Category category;

    private BigDecimal price;

    private String imageUrl;

    private String screenshotUrl;

    @Column(length = 2000)
    private String memo;

    @Column(nullable = false)
    private Boolean isPurchased;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private Item(Long userId, String productName, String brand, Category category,
                 BigDecimal price, String imageUrl, String screenshotUrl, String memo) {
        this.userId = userId;
        this.productName = productName;
        this.brand = brand;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
        this.screenshotUrl = screenshotUrl;
        this.memo = memo;
        this.isPurchased = false;
    }

    public static Item of(Long userId, String productName, String brand, Category category,
                          BigDecimal price, String imageUrl, String screenshotUrl, String memo) {
        return new Item(userId, productName, brand, category, price, imageUrl, screenshotUrl, memo);
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void togglePurchased() {
        this.isPurchased = !this.isPurchased;
    }
}

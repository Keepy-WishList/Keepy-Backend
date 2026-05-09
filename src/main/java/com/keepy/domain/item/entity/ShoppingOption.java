package com.keepy.domain.item.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "shopping_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShoppingOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private String siteName;

    @Column(nullable = false, length = 1000)
    private String siteUrl;

    private BigDecimal price;

    private String deliveryFee;

    private ShoppingOption(Long itemId, String siteName, String siteUrl, BigDecimal price,
                           String deliveryFee) {
        this.itemId = itemId;
        this.siteName = siteName;
        this.siteUrl = siteUrl;
        this.price = price;
        this.deliveryFee = deliveryFee;
    }

    public static ShoppingOption of(Long itemId, String siteName, String siteUrl, BigDecimal price,
                                    String deliveryFee) {
        return new ShoppingOption(itemId, siteName, siteUrl, price, deliveryFee);
    }
}

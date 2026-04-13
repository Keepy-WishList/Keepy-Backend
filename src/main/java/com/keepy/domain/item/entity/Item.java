package com.keepy.domain.item.entity;

import com.keepy.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "items")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String productName;

    private String brand;

    @Enumerated(EnumType.STRING)
    private Category category;

    private BigDecimal price;

    private String currency;

    // 분석된 제품 이미지 URL (S3)
    private String imageUrl;

    // 사용자가 올린 원본 스크린샷 URL (S3)
    private String screenshotUrl;

    @Column(length = 1000)
    private String description;

    @Column(length = 2000)
    private String memo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPurchased = false;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShoppingOption> shoppingOptions = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void togglePurchased() {
        this.isPurchased = !this.isPurchased;
    }

    public void addShoppingOption(ShoppingOption option) {
        shoppingOptions.add(option);
    }
}

package com.keepy.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    private String providerId;

    @Column(nullable = false)
    private int analysisCount = 0;

    private LocalDateTime analysisWindowStart;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private User(String email, String name, AuthProvider provider, String providerId) {
        this.email = email;
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
    }

    public static User of(String email, String name, AuthProvider provider, String providerId) {
        return new User(email, name, provider, providerId);
    }

    public void updateProfile(String name) {
        if (name != null) this.name = name;
    }

    public int getRemainingAnalysisCount() {
        if (analysisWindowStart == null || LocalDateTime.now().isAfter(analysisWindowStart.plusHours(1))) {
            return 3;
        }
        return Math.max(0, 3 - analysisCount);
    }

    public void incrementAnalysisCount() {
        LocalDateTime now = LocalDateTime.now();
        if (analysisWindowStart == null || now.isAfter(analysisWindowStart.plusHours(1))) {
            this.analysisWindowStart = now;
            this.analysisCount = 1;
        } else {
            this.analysisCount++;
        }
    }

    public boolean isAnalysisLimitExceeded() {
        if (analysisWindowStart == null || LocalDateTime.now().isAfter(analysisWindowStart.plusHours(1))) {
            return false;
        }
        return analysisCount >= 3;
    }
}

package com.keepy.domain.user.dto;

import com.keepy.domain.user.entity.User;

public record UserProfileResponse(
        Long id,
        String email,
        String name,
        String provider
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProvider().name()
        );
    }
}

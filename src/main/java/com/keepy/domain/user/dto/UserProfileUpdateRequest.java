package com.keepy.domain.user.dto;

public record UserProfileUpdateRequest(
        String name,
        String profileImageUrl
) {}

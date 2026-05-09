package com.keepy.domain.user.controller;

import com.keepy.domain.user.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "User", description = "사용자 프로필 API")
public interface UserApiSpecification {

    @Operation(
            summary = "내 프로필 조회",
            description = "로그인한 사용자의 프로필 정보(이메일, 이름, 소셜 로그인 제공자)를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<com.keepy.global.common.ApiResponse<UserProfileResponse>> getProfile(
            @Parameter(hidden = true) UserDetails userDetails
    );
}

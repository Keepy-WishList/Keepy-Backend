package com.keepy.domain.user.controller;

import com.keepy.domain.user.dto.UserProfileResponse;
import com.keepy.domain.user.dto.UserProfileUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;

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

    @Operation(
            summary = "프로필 수정",
            description = "이름을 수정합니다. 변경하지 않을 필드는 null로 전달하세요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<com.keepy.global.common.ApiResponse<UserProfileResponse>> updateProfile(
            @Parameter(hidden = true) UserDetails userDetails,
            @RequestBody UserProfileUpdateRequest request
    );
}

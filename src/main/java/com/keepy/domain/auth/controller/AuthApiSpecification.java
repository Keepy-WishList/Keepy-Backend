package com.keepy.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "Auth", description = "인증 API — 토큰 갱신·로그아웃. OAuth 로그인은 /oauth2/authorization/{provider} 를 사용합니다.")
public interface AuthApiSpecification {

    @Operation(
            summary = "토큰 갱신",
            description = "refresh_token 쿠키를 사용해 access_token과 refresh_token을 재발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "refresh_token 없음 또는 만료됨")
    })
    ResponseEntity<com.keepy.global.common.ApiResponse<Void>> refresh(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            summary = "로그아웃",
            description = "DB의 refresh_token을 삭제하고 access_token·refresh_token 쿠키를 만료시킵니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<com.keepy.global.common.ApiResponse<Void>> logout(
            @Parameter(hidden = true) UserDetails userDetails,
            @Parameter(hidden = true) HttpServletResponse response
    );
}

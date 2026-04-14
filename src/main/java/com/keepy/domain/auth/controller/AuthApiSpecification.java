package com.keepy.domain.auth.controller;

import com.keepy.domain.auth.dto.LoginRequest;
import com.keepy.domain.auth.dto.SignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "인증 API — 회원가입·로그인·토큰 갱신·로그아웃. 토큰은 HttpOnly 쿠키(access_token / refresh_token)로 주고받습니다.")
public interface AuthApiSpecification {

    @Operation(
            summary = "회원가입",
            description = "이메일·비밀번호로 신규 계정을 생성합니다. 성공 시 access_token, refresh_token 쿠키가 자동 설정됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 오류"),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일")
    })
    ResponseEntity<com.keepy.global.common.ApiResponse<Void>> signup(
            @Valid @RequestBody SignupRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            summary = "로그인",
            description = "이메일·비밀번호로 로그인합니다. 성공 시 access_token, refresh_token 쿠키가 자동 설정됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 오류"),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    })
    ResponseEntity<com.keepy.global.common.ApiResponse<Void>> login(
            @Valid @RequestBody LoginRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            summary = "토큰 갱신",
            description = "refresh_token 쿠키를 사용해 access_token과 refresh_token을 재발급합니다. 요청 시 refresh_token 쿠키가 필요합니다."
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

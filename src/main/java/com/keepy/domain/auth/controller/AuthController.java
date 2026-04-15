package com.keepy.domain.auth.controller;

import com.keepy.domain.auth.dto.TokenResponse;
import com.keepy.domain.auth.service.AuthService;
import com.keepy.global.common.ApiResponse;
import com.keepy.global.exception.CustomException;
import com.keepy.global.exception.ErrorCode;
import com.keepy.global.security.TokenCookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApiSpecification {

    private final AuthService authService;
    private final TokenCookieHelper tokenCookieHelper;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = tokenCookieHelper.resolveRefreshToken(request)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
        TokenResponse tokens = authService.refresh(refreshToken);
        tokenCookieHelper.setTokenCookies(response, tokens);
        return ResponseEntity.ok(ApiResponse.success("토큰이 갱신되었습니다."));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response) {
        Long userId = Long.parseLong(userDetails.getUsername());
        authService.logout(userId);
        tokenCookieHelper.clearTokenCookies(response);
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다."));
    }
}

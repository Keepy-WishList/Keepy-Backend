package com.keepy.global.security;

import com.keepy.domain.auth.dto.TokenResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class TokenCookieHelper {

    public static final String ACCESS_TOKEN_COOKIE  = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    public static final String REFRESH_TOKEN_PATH   = "/api/auth/refresh";

    @Value("${app.cookie.secure}")
    private boolean secure;

    @Value("${jwt.access-token-validity-in-seconds}")
    private long accessTokenValidityMs;   // 값이 ms 단위

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityMs;  // 값이 ms 단위

    public void setTokenCookies(HttpServletResponse response, TokenResponse tokens) {
        addCookie(response, ACCESS_TOKEN_COOKIE,  tokens.accessToken(),  "/",
                (int) (accessTokenValidityMs / 1000));
        addCookie(response, REFRESH_TOKEN_COOKIE, tokens.refreshToken(), REFRESH_TOKEN_PATH,
                (int) (refreshTokenValidityMs / 1000));
    }

    public void clearTokenCookies(HttpServletResponse response) {
        expireCookie(response, ACCESS_TOKEN_COOKIE,  "/");
        expireCookie(response, REFRESH_TOKEN_COOKIE, REFRESH_TOKEN_PATH);
    }

    public Optional<String> resolveAccessToken(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE);
    }

    public Optional<String> resolveRefreshToken(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE);
    }

    // ── private ─────────────────────────────────────────────────────────────

    private void addCookie(HttpServletResponse response, String name, String value,
                           String path, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(path)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void expireCookie(HttpServletResponse response, String name, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(path)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private Optional<String> getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue);
    }
}

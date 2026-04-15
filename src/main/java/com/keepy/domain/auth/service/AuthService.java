package com.keepy.domain.auth.service;

import com.keepy.domain.auth.dto.TokenResponse;
import com.keepy.domain.auth.entity.RefreshToken;
import com.keepy.domain.auth.repository.RefreshTokenRepository;
import com.keepy.global.exception.CustomException;
import com.keepy.global.exception.ErrorCode;
import com.keepy.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityMs;

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        RefreshToken saved = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (saved.isExpired()) {
            refreshTokenRepository.delete(saved);
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }

        return issueTokens(saved.getUserId());
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public TokenResponse issueTokens(Long userId) {
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(refreshTokenValidityMs / 1000);

        refreshTokenRepository.findByUserId(userId).ifPresentOrElse(
                token -> token.update(refreshToken, expiresAt),
                () -> refreshTokenRepository.save(
                        RefreshToken.of(userId, refreshToken, expiresAt)
                )
        );

        return TokenResponse.of(accessToken, refreshToken);
    }
}

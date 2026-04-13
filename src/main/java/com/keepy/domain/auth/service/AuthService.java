package com.keepy.domain.auth.service;

import com.keepy.domain.auth.dto.LoginRequest;
import com.keepy.domain.auth.dto.SignupRequest;
import com.keepy.domain.auth.dto.TokenRefreshRequest;
import com.keepy.domain.auth.dto.TokenResponse;
import com.keepy.domain.auth.entity.RefreshToken;
import com.keepy.domain.auth.repository.RefreshTokenRepository;
import com.keepy.domain.user.entity.AuthProvider;
import com.keepy.domain.user.entity.User;
import com.keepy.domain.user.repository.UserRepository;
import com.keepy.global.exception.CustomException;
import com.keepy.global.exception.ErrorCode;
import com.keepy.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityMs;

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .provider(AuthProvider.LOCAL)
                .build();

        userRepository.save(user);
        return issueTokens(user.getId());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (user.getPassword() == null ||
                !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(user.getId());
    }

    @Transactional
    public TokenResponse refresh(TokenRefreshRequest request) {
        if (!jwtTokenProvider.validateToken(request.refreshToken())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        RefreshToken saved = refreshTokenRepository.findByToken(request.refreshToken())
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
                        RefreshToken.builder()
                                .userId(userId)
                                .token(refreshToken)
                                .expiresAt(expiresAt)
                                .build()
                )
        );

        return TokenResponse.of(accessToken, refreshToken);
    }
}

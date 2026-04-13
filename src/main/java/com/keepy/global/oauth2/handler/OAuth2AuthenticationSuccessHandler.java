package com.keepy.global.oauth2.handler;

import com.keepy.domain.auth.dto.TokenResponse;
import com.keepy.domain.auth.service.AuthService;
import com.keepy.global.oauth2.userinfo.CustomOAuth2UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2UserPrincipal principal = (CustomOAuth2UserPrincipal) authentication.getPrincipal();
        TokenResponse tokens = authService.issueTokens(principal.getUserId());

        String redirectUri = UriComponentsBuilder.fromUriString("keepy://auth/callback")
                .queryParam("token", tokens.accessToken())
                .queryParam("refreshToken", tokens.refreshToken())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}

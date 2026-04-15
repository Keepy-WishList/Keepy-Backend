package com.keepy.global.oauth2.handler;

import com.keepy.domain.auth.dto.TokenResponse;
import com.keepy.domain.auth.service.AuthService;
import com.keepy.global.oauth2.userinfo.CustomOAuth2UserPrincipal;
import com.keepy.global.security.TokenCookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final TokenCookieHelper tokenCookieHelper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2UserPrincipal principal = (CustomOAuth2UserPrincipal) authentication.getPrincipal();
        TokenResponse tokens = authService.issueTokens(principal.getUserId());

        tokenCookieHelper.setTokenCookies(response, tokens);
        getRedirectStrategy().sendRedirect(request, response, "http://localhost:3000/auth/callback");
    }
}

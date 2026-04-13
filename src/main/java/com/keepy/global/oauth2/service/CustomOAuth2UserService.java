package com.keepy.global.oauth2.service;

import com.keepy.domain.user.entity.AuthProvider;
import com.keepy.domain.user.entity.User;
import com.keepy.domain.user.repository.UserRepository;
import com.keepy.global.oauth2.userinfo.CustomOAuth2UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        String providerId = (String) attributes.get("sub");

        User user = userRepository.findByEmail(email)
                .map(existing -> {
                    existing.updateProfile(name, picture);
                    return existing;
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .profileImageUrl(picture)
                                .provider(provider)
                                .providerId(providerId)
                                .build()
                ));

        return new CustomOAuth2UserPrincipal(user.getId(), attributes);
    }
}

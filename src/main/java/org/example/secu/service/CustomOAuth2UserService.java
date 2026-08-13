package org.example.secu.service;

import lombok.RequiredArgsConstructor;
import org.example.secu.domain.dto.CustomOAuth2User;
import org.example.secu.domain.dto.KakaoOAuth2DTO;
import org.example.secu.domain.entity.UserAccountEntity;
import org.example.secu.domain.repository.UserAccountRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    // 1. 로그인
    // 2. 로그인 -> 실패 -> 계정 없음 -> 새롭게 생성

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User ou = super.loadUser(userRequest); // Provider를 통해서 받은 정보
        // 상속 받은 원본(상위) 클래스에서 이미 구현된 loadUser로 OAuth2User

        // 1. OAuth2User -> DB에 있는 UserAccountEntity를 조회하거나 생성

        // provider 이름
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        // kakao, google
        Map<String, Object> attributes = ou.getAttributes(); // provider마다 형태가 다름

        String providerId; // provider에서 제공해주는 식별자
//        String nickname;

        ObjectMapper objectMapper = new ObjectMapper();

        switch (registrationId) {
            case "kakao" -> {
                // JSON -> Map => DTO
                KakaoOAuth2DTO kakaoOAuth2DTO = objectMapper.convertValue(attributes, KakaoOAuth2DTO.class);
                providerId = kakaoOAuth2DTO.id();
                // nickname = kakaoOAuth2DTO.kakao_account().profile().nickname();
                // profile_image, email...
            }
            // google...
            default -> {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("unsupported provider"),
                        "지원하지 않는 프로바이더 : %s".formatted(registrationId)
                );
            }
        }

        // 로그인 시도
        UserAccountEntity userAccount;
        try {
            userAccount = userAccountRepository
                    .findBySocialIdAndSocialProvider(providerId, registrationId)
                    .orElseThrow(); // 없으면? -> Throw -> catch
        } catch (NoSuchElementException e) {
            UserAccountEntity oUser = UserAccountEntity.builder()
                    .socialId(providerId)
                    .socialProvider(registrationId)
                    // kakao_******
                    .username("%s_%s".formatted(providerId, registrationId))
                    // .password ...
                    .role("user")
                    .build();
            userAccount = userAccountRepository.save(oUser);
            // 가입 처리
        }
//        return super.loadUser(userRequest);
        return new CustomOAuth2User(userAccount, attributes);
        // OAuth2User / CustomUserDetails
        // -> Spring Security -> loadUsername -> UserAccount.
    }

    private final UserAccountRepository userAccountRepository;
}

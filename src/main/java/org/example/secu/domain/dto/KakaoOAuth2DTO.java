package org.example.secu.domain.dto;

public record KakaoOAuth2DTO(String id,
                             KakaoAccount kakao_account) {
    public record KakaoAccount(Profile profile) {
    }

    public record Profile(String nickname) {
    }
}

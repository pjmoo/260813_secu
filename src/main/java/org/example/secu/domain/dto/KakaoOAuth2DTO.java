package org.example.secu.domain.dto;

public record KakaoOAuth2DTO(String id, KakaoAccount kakaoAccount) {
    public record KakaoAccount(Profile profile) {
    }

    public record Profile(String nickname) {
    }
}

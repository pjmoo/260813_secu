package org.example.secu.domain.dto;

import lombok.Getter;
import org.example.secu.domain.entity.UserAccountEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

@Getter
public class CustomOAuth2User extends CustomUserDetails implements OAuth2User {
    private final Map<String, Object> attributes;

    public CustomOAuth2User(UserAccountEntity account, Map<String, Object> attributes) {
        super(account);
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(getId());
    }
}

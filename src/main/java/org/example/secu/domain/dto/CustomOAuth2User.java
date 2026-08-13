package org.example.secu.domain.dto;

import lombok.Getter;
import org.example.secu.domain.entity.UserAccountEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

@Getter
public class CustomOAuth2User extends CustomUserDetails implements OAuth2User {
    // CustomOAuth2User -> OAuth2User -> UserDetails
    // OAuth2Client도 처리할 수 있고, Spring Security도 처리할 수 있는 타입이 되었음
    private final Map<String, Object> attributes;

    public CustomOAuth2User(UserAccountEntity account, Map<String, Object> attributes) {
        super(account); // account의 속성을 하나하나 넣으면 어려우니까 별도의 Account를 넣어서 만드는 생성자를 신규로 만들어줌
        this.attributes = attributes;
    }

    @Override
    public String getName() { // -> id로 넣어도 되고, username로 넣어도 된다...
        // -> Controller에서 쓰는 건 CustomUserDetails.getUsername()
//        return String.valueOf(getId());
        return String.valueOf(getUsername());
    }
}

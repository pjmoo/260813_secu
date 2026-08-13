package org.example.secu.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.secu.domain.entity.UserAccountEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final List<SimpleGrantedAuthority> authorities;
    private final long id;
    private final String username;
    private final String password;

    public boolean isAdmin() {
        return authorities.stream()
                .anyMatch(a -> a.getAuthority()
                        .equals("ROLE_ADMIN"));
    }

    public CustomUserDetails(UserAccountEntity account) {
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_%s"
                .formatted(account.getRole().toUpperCase())));
        this.id = account.getId();
        this.username = account.getUsername();
        this.password = account.getPassword();
    }
}

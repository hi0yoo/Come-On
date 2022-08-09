package com.comeon.authservice.auth.oauth.entity;

import com.comeon.authservice.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2UserAdaptor extends DefaultOAuth2User {

    private final User user;

    public CustomOAuth2UserAdaptor(
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes,
            String nameAttributeKey,
            User user) {
        super(authorities, attributes, nameAttributeKey);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}

package com.comeon.authservice.common.auth.oauth.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2UserAdaptor extends DefaultOAuth2User {

    private final Long userId;

    public CustomOAuth2UserAdaptor(
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes,
            String nameAttributeKey,
            Long userId) {
        super(authorities, attributes, nameAttributeKey);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}

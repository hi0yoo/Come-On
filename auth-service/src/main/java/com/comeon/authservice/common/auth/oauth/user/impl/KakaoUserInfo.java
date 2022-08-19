package com.comeon.authservice.common.auth.oauth.user.impl;

import com.comeon.authservice.common.auth.oauth.user.OAuth2UserInfo;

import java.util.Map;

public class KakaoUserInfo extends OAuth2UserInfo {

    private static final String PROVIDER_NAME = "KAKAO";

    public KakaoUserInfo(Map<String, Object> userAttributes) {
        super(userAttributes);
    }

    @Override
    public String getOAuthId() {
        return this.userAttributes.get("id").toString();
    }

    @Override
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) this.userAttributes.get("properties");

        if (properties == null) {
            return null;
        }

        return (String) properties.get("nickname");
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) this.userAttributes.get("kakao_account");

        if (kakaoAccount == null) {
            return null;
        }

        return  (String) kakaoAccount.get("email");
    }

    @Override
    public String getProfileImgUrl() {
        Map<String, Object> properties = (Map<String, Object>) this.userAttributes.get("properties");

        if (properties == null) {
            return null;
        }

        return (String) properties.get("thumbnail_image");
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
}

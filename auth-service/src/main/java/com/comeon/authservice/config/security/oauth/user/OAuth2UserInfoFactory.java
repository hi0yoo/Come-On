package com.comeon.authservice.config.security.oauth.user;

import com.comeon.authservice.config.security.oauth.user.impl.KakaoUserInfo;

import java.util.Map;

public class OAuth2UserInfoFactory {

    // Provider 에 맞추어 OAuthUserInfo 구현체 생성
    public static OAuth2UserInfo getOAuthUserInfo(String providerName, Map<String, Object> userAttributes) {
        switch (providerName.toUpperCase()) {
            case "KAKAO": return new KakaoUserInfo(userAttributes);

            default: throw new IllegalArgumentException("지원하지 않는 Provider 입니다.");
        }
    }
}

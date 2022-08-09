package com.comeon.authservice.auth.oauth.service;

import com.comeon.authservice.auth.oauth.user.OAuth2UserInfoFactory;
import com.comeon.authservice.domain.user.dto.UserDto;
import com.comeon.authservice.domain.user.entity.User;
import com.comeon.authservice.domain.user.service.UserService;
import com.comeon.authservice.auth.oauth.entity.CustomOAuth2UserAdaptor;
import com.comeon.authservice.auth.oauth.user.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Provider 로부터 사용자 정보 받아오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Provider 마다 제공한 attribute key 가 다르다.
        // 공통된 OAuthUserInfo 객체로 변환한다.
        String providerName = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> userAttributes = oAuth2User.getAttributes();
        OAuth2UserInfo oAuthUserInfo = OAuth2UserInfoFactory.getOAuthUserInfo(
                providerName,
                userAttributes
        );
        UserDto userDto = new UserDto(
                oAuthUserInfo.getProviderName(),
                oAuthUserInfo.getOAuthId(),
                oAuthUserInfo.getEmail(),
                oAuthUserInfo.getName(),
                oAuthUserInfo.getProfileImgUrl()
        );

        // 사용자 정보를 저장, 수정한다.
        User user = userService.saveUser(userDto);

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        return new CustomOAuth2UserAdaptor(
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getRoleValue())),
                userAttributes,
                userNameAttributeName,
                user
        );
    }
}

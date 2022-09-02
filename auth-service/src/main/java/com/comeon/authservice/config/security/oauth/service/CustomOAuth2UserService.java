package com.comeon.authservice.config.security.oauth.service;

import com.comeon.authservice.config.security.oauth.user.OAuth2UserInfoFactory;
import com.comeon.authservice.config.security.oauth.entity.CustomOAuth2UserAdaptor;
import com.comeon.authservice.config.security.oauth.user.OAuth2UserInfo;
import com.comeon.authservice.feign.userservice.request.UserSaveRequest;
import com.comeon.authservice.feign.userservice.response.UserSaveResponse;
import com.comeon.authservice.feign.userservice.UserServiceFeignClient;
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

    private final UserServiceFeignClient userServiceFeignClient;

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

        UserSaveRequest request = new UserSaveRequest(
                oAuthUserInfo.getOAuthId(),
                oAuthUserInfo.getProviderName(),
                oAuthUserInfo.getName(),
                oAuthUserInfo.getEmail(),
                oAuthUserInfo.getProfileImgUrl()
        );

        // 사용자 정보를 저장, 수정한다.
        // TODO 예외 처리
        UserSaveResponse response = userServiceFeignClient.saveUser(request).getData();

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        return new CustomOAuth2UserAdaptor(
                Collections.singletonList(new SimpleGrantedAuthority(response.getRole())),
                userAttributes,
                userNameAttributeName,
                response.getUserId()
        );
    }
}

package com.comeon.authservice.config.security.handler;

import com.comeon.authservice.common.exception.CustomException;
import com.comeon.authservice.common.exception.ErrorCode;
import com.comeon.authservice.common.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.comeon.authservice.common.utils.CookieUtil.COOKIE_NAME_USER_LOGOUT_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LogoutHandler implements LogoutHandler {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        /*
            - token : 엑세스 토큰
            - redirect_uri : 로그아웃 완료시 프론트측 리다이렉트 주소
         */
        String token = request.getParameter("token");
        String frontRedirectUri = request.getParameter("redirect_uri");
        UserLogoutRequest userLogoutRequest = new UserLogoutRequest(token, frontRedirectUri);
        // 추후 애플리케이션 서버 로그아웃에 사용
        CookieUtil.addSecureCookie(response, COOKIE_NAME_USER_LOGOUT_REQUEST, CookieUtil.serialize(userLogoutRequest), 60);

        String serverLogoutEndpoint = "https://api.come-on.ml/auth/logout";

        String kakaoLogoutRequestUri = UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/logout")
                .queryParam("client_id", kakaoClientId)
                .queryParam("logout_redirect_uri", serverLogoutEndpoint)
                .build()
                .toUriString();

        try {
            response.sendRedirect(kakaoLogoutRequestUri);
        } catch (IOException e) {
            throw new CustomException(e, ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}

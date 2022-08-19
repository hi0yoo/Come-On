package com.comeon.authservice.common.auth.oauth.repository;

import com.comeon.authservice.common.utils.CookieUtil;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.comeon.authservice.common.utils.CookieUtil.COOKIE_NAME_OAUTH2_AUTHORIZATION_REQUEST;
import static com.comeon.authservice.common.utils.CookieUtil.COOKIE_NAME_REDIRECT_URI;

@Component
public class CustomAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        // OAuth2 로그인 요청이 들어오면, 요청을 기반으로 "OAuth2AuthorizationRequest"를 생성한다.
        // "OAuth2AuthorizationRequest"에는 OAuth2와 관련된 정보가 포함되어 있다.
        // "OAuth2AuthorizationRequest" 객체를 쿠키로 변환하여 OAuth2 인증 과정동안 keep 해둔다.

        if (authorizationRequest == null) {
            CookieUtil.deleteCookie(request, response, COOKIE_NAME_OAUTH2_AUTHORIZATION_REQUEST);
            CookieUtil.deleteCookie(request, response, COOKIE_NAME_REDIRECT_URI);
            return;
        }

        CookieUtil.addCookie(response, COOKIE_NAME_OAUTH2_AUTHORIZATION_REQUEST, CookieUtil.serialize(authorizationRequest), 60);
        // OAuth2 로그인 성공시 redirect 할 uri
        CookieUtil.addCookie(response, COOKIE_NAME_REDIRECT_URI, request.getParameter("redirect_uri"), 60);
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        // OAuth2 인증 과정을 마치면, keep 해둔 쿠키를 바탕으로 다시 "OAuth2AuthorizationRequest" 객체로 변환한다.
        return CookieUtil.getCookie(request, COOKIE_NAME_OAUTH2_AUTHORIZATION_REQUEST)
                .map(cookie -> CookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class))
                .orElse(null);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
        return this.loadAuthorizationRequest(request);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        return this.loadAuthorizationRequest(request);
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, COOKIE_NAME_OAUTH2_AUTHORIZATION_REQUEST);
        CookieUtil.deleteCookie(request, response, COOKIE_NAME_REDIRECT_URI);
    }
}

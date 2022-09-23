package com.comeon.authservice.config.security.oauth.handler;

import com.comeon.authservice.common.jwt.JwtTokenInfo;
import com.comeon.authservice.common.jwt.JwtTokenProvider;
import com.comeon.authservice.config.security.oauth.repository.CustomAuthorizationRequestRepository;
import com.comeon.authservice.common.jwt.JwtRepository;
import com.comeon.authservice.config.security.oauth.entity.CustomOAuth2UserAdaptor;
import com.comeon.authservice.common.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;

import static com.comeon.authservice.common.utils.CookieUtil.COOKIE_NAME_REDIRECT_URI;
import static com.comeon.authservice.common.utils.CookieUtil.COOKIE_NAME_REFRESH_TOKEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final CustomAuthorizationRequestRepository authorizationRequestRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRepository jwtRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2UserAdaptor oAuth2User = (CustomOAuth2UserAdaptor) authentication.getPrincipal();

        // access 토큰 생성
        Long userId = oAuth2User.getUserId();
        log.info("[login-success] UserId : {}", userId);
        JwtTokenInfo accessToken = jwtTokenProvider.createAccessToken(userId.toString(), authentication);
        log.info("[create-token] AccessToken : {}", accessToken.getValue());

        // refresh 토큰 생성 및 저장
        JwtTokenInfo refreshToken = jwtTokenProvider.createRefreshToken();
        Duration refreshTokenDuration = Duration.between(Instant.now(), refreshToken.getExpiry());
        log.info("[create-token] RefreshToken : {}", refreshToken.getValue());
        String refreshTokenValue = refreshToken.getValue();

        jwtRepository.addRefreshToken(
                userId.toString(),
                refreshTokenValue,
                refreshTokenDuration
        );

        String redirectUri = CookieUtil.getCookie(request, COOKIE_NAME_REDIRECT_URI)
                .map(Cookie::getValue)
                .orElse(getDefaultTargetUrl());

        CookieUtil.deleteCookie(
                request,
                response,
                COOKIE_NAME_REFRESH_TOKEN
        );
        CookieUtil.addCookie(
                response,
                COOKIE_NAME_REFRESH_TOKEN,
                refreshTokenValue,
                Long.valueOf(refreshTokenDuration.getSeconds()).intValue(),
                getDomainName(redirectUri)
        );

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", accessToken.getValue())
                .queryParam("expiry", accessToken.getExpiry().getEpochSecond())
                .queryParam("userId", userId)
                .build().toUriString();

        // TODO 쿠키 응답 이슈
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Set-Cookie");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, String.valueOf(true));

        // auth 과정에서 생성한 session 비우기
        super.clearAuthenticationAttributes(request);
        // auth 과정에서 생성한 쿠키들 삭제
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        log.info("[login-success] Send Redirect. URL : {}", redirectUri);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String getDomainName(String url) throws MalformedURLException {
        if (!url.startsWith("http") && !url.startsWith("https")) {
            url = "http://" + url;
        }
        URL netUrl = new URL(url);
        String host = netUrl.getHost();
        if (host.startsWith("www")) {
            host = host.substring("www".length() + 1);
        }
        return host;
    }
}

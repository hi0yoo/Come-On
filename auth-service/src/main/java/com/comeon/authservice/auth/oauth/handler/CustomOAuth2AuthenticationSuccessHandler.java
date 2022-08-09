package com.comeon.authservice.auth.oauth.handler;

import com.comeon.authservice.auth.jwt.JwtTokenProvider;
import com.comeon.authservice.auth.oauth.repository.CustomAuthorizationRequestRepository;
import com.comeon.authservice.domain.refreshtoken.dto.RefreshTokenDto;
import com.comeon.authservice.domain.refreshtoken.service.RefreshTokenService;
import com.comeon.authservice.domain.user.entity.User;
import com.comeon.authservice.auth.oauth.entity.CustomOAuth2UserAdaptor;
import com.comeon.authservice.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.comeon.authservice.utils.CookieUtil.COOKIE_NAME_REDIRECT_URI;
import static com.comeon.authservice.utils.CookieUtil.COOKIE_NAME_REFRESH_TOKEN;

@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${jwt.refresh-token.expire-time}")
    private long refreshTokenExpirySec;

    private final CustomAuthorizationRequestRepository authorizationRequestRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2UserAdaptor oAuth2User = (CustomOAuth2UserAdaptor) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        // access 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId().toString(), authentication);

        // refresh 토큰 생성 및 저장
        String refreshToken = jwtTokenProvider.createRefreshToken();
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(user, refreshToken);
        refreshTokenService.saveRefreshToken(refreshTokenDto);

        CookieUtil.deleteCookie(request, response, COOKIE_NAME_REFRESH_TOKEN);
        CookieUtil.addCookie(response, COOKIE_NAME_REFRESH_TOKEN, refreshToken, Long.valueOf(refreshTokenExpirySec).intValue());

        String redirectUri = CookieUtil.getCookie(request, COOKIE_NAME_REDIRECT_URI)
                .map(Cookie::getValue)
                .orElse(getDefaultTargetUrl());

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", accessToken)
                .build().toUriString();

        // auth 과정에서 생성한 session 비우기
        super.clearAuthenticationAttributes(request);
        // auth 과정에서 생성한 쿠키들 삭제
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

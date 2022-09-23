package com.comeon.authservice.config.security.handler;

import com.comeon.authservice.common.exception.CustomException;
import com.comeon.authservice.common.exception.ErrorCode;
import com.comeon.authservice.common.jwt.JwtTokenProvider;
import com.comeon.authservice.common.jwt.RedisRepository;
import com.comeon.authservice.common.utils.CookieUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import static com.comeon.authservice.common.exception.ErrorCode.*;
import static com.comeon.authservice.common.utils.CookieUtil.COOKIE_NAME_REFRESH_TOKEN;
import static com.comeon.authservice.common.utils.CookieUtil.COOKIE_NAME_USER_LOGOUT_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisRepository jwtRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        UserLogoutRequest userLogoutRequest = resolveLogoutRequest(request);

        checkAccessTokenIsValid(userLogoutRequest);

        String accessToken = userLogoutRequest.getAccessToken();
        Instant expiration = jwtTokenProvider.getClaims(accessToken).getExpiration().toInstant();
        // 블랙 리스트에 추가. duration 만큼 지나면 자동 삭제.
        jwtRepository.addBlackList(accessToken, Duration.between(Instant.now(), expiration));
        // RefreshToken 삭제
        jwtRepository.removeRefreshToken(jwtTokenProvider.getUserId(accessToken));

        // 리프레시 토큰, 로그아웃 요청 쿠키 삭제
        CookieUtil.deleteSecureCookie(request, response, COOKIE_NAME_USER_LOGOUT_REQUEST);
        CookieUtil.deleteSecureCookie(request, response, COOKIE_NAME_REFRESH_TOKEN);

        String uriString = UriComponentsBuilder.fromUriString(userLogoutRequest.getFrontRedirectUri())
                .build().toUriString();
        try {
            response.sendRedirect(uriString);
        } catch (IOException e) {
            throw new CustomException(e, ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private UserLogoutRequest resolveLogoutRequest(HttpServletRequest request) {
        UserLogoutRequest userLogoutRequest = CookieUtil.getCookie(request, COOKIE_NAME_USER_LOGOUT_REQUEST)
                .map(cookie -> CookieUtil.deserialize(cookie, UserLogoutRequest.class))
                .orElse(
                        new UserLogoutRequest(
                                request.getParameter("token"),
                                request.getParameter("redirect_uri")
                        )
                );

        checkAccessTokenInRequest(userLogoutRequest);
        checkFrontRedirectUriInRequest(userLogoutRequest);

        return userLogoutRequest;
    }

    private void checkAccessTokenIsValid(UserLogoutRequest userLogoutRequest) {
        String accessToken = userLogoutRequest.getAccessToken();
        if (jwtRepository.findBlackList(accessToken).isPresent()) {
            throw new CustomException("이미 로그아웃 처리된 엑세스 토큰 입니다. userId : " + jwtTokenProvider.getUserId(accessToken), INVALID_ACCESS_TOKEN);
        }

        try {
            jwtTokenProvider.validate(accessToken);
        } catch (JwtException e) {
            throw new CustomException("엑세스 토큰 검증이 실패하였습니다.", e, INVALID_ACCESS_TOKEN);
        }
    }

    private void checkAccessTokenInRequest(UserLogoutRequest userLogoutRequest) {
        if (Objects.isNull(userLogoutRequest.getAccessToken())) {
            throw new CustomException("요청에 엑세스 토큰이 없어서 로그아웃을 진행할 수 없습니다.", NO_PARAM_TOKEN);
        }
    }

    private void checkFrontRedirectUriInRequest(UserLogoutRequest userLogoutRequest) {
        if (Objects.isNull(userLogoutRequest.getFrontRedirectUri())) {
            throw new CustomException("요청에 리다이렉트 할 URI가 없어서 로그아웃을 진행할 수 없습니다.", NO_PARAM_REDIRECT_URI);
        }
    }
}

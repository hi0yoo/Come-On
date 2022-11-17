package com.comeon.authservice.web.auth.service;

import com.comeon.authservice.common.jwt.JwtTokenProvider;
import com.comeon.authservice.common.jwt.RedisRepository;
import com.comeon.authservice.common.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;

import static com.comeon.authservice.common.utils.CookieUtil.COOKIE_NAME_REFRESH_TOKEN;
import static com.comeon.authservice.common.utils.CookieUtil.COOKIE_NAME_USER_LOGOUT_REQUEST;

@Component
@RequiredArgsConstructor
public class LogoutManager {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisRepository redisRepository;

    public void doAppLogout(HttpServletRequest request, HttpServletResponse response, String accessToken) {
        Instant expiration = jwtTokenProvider.getClaims(accessToken).getExpiration().toInstant();
        // 블랙 리스트에 추가. duration 만큼 지나면 자동 삭제.
        redisRepository.addBlackList(accessToken, Duration.between(Instant.now(), expiration));
        // RefreshToken 삭제
        redisRepository.removeRefreshToken(jwtTokenProvider.getUserId(accessToken));

        // 리프레시 토큰, 로그아웃 요청 쿠키 삭제
        CookieUtil.deleteSecureCookie(request, response, COOKIE_NAME_USER_LOGOUT_REQUEST);
        CookieUtil.deleteSecureCookie(request, response, COOKIE_NAME_REFRESH_TOKEN);
    }
}

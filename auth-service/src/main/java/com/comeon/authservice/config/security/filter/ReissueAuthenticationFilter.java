package com.comeon.authservice.config.security.filter;

import com.comeon.authservice.common.exception.CustomException;
import com.comeon.authservice.common.jwt.JwtTokenProvider;
import com.comeon.authservice.common.jwt.JwtRepository;
import com.comeon.authservice.common.utils.CookieUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.comeon.authservice.common.exception.ErrorCode.*;

@Slf4j
@RequiredArgsConstructor
public class ReissueAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRepository jwtRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String accessToken = resolveAccessToken(request);

        if (!StringUtils.hasText(accessToken)) {
            throw new CustomException("인증 헤더에서 AccessToken을 찾을 수 없습니다.", NOT_EXIST_AUTHORIZATION_HEADER);
        }

        // AccessToken 블랙리스트 확인 -> 블랙리스트에 있으면 거부
        if (jwtRepository.findBlackList(accessToken).isPresent()) {
            throw new CustomException("로그아웃 처리된 Access Token 입니다.", INVALID_ACCESS_TOKEN);
        }

        if (!isAccessTokenExpired(accessToken)) {
            throw new CustomException("Access Token이 만료되지 않아 재발급 할 수 없습니다.", NOT_EXPIRED_ACCESS_TOKEN);
        }

        String refreshToken = CookieUtil.getCookie(request, CookieUtil.COOKIE_NAME_REFRESH_TOKEN)
                .map(Cookie::getValue)
                .orElseThrow(
                        () -> new CustomException("Refresh Token이 존재하지 않습니다.", NOT_EXIST_REFRESH_TOKEN)
                );

        // Redis에 RT가 없으면, 전달받은 RT와 저장된 RT가 다르면, 유효하지 않은 리프레시 토큰
        refreshToken = jwtRepository.findRefreshTokenByUserId(jwtTokenProvider.getUserId(accessToken))
                .filter(refreshToken::equals)
                .orElseThrow(
                        () -> new CustomException("Refresh Token이 저장된 값과 다릅니다.", INVALID_REFRESH_TOKEN)
                );

        // RefreshToken 검증에 실패하면 예외 발생
        try {
            jwtTokenProvider.validate(refreshToken);
        } catch (JwtException e) {
            throw new CustomException("유효하지 않은 Refresh Token 입니다.", e, INVALID_REFRESH_TOKEN);
        }

        // 다음 필터 수행
        filterChain.doFilter(request, response);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    private boolean isAccessTokenExpired(String accessToken) {
        try {
            jwtTokenProvider.validate(accessToken);
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            throw new CustomException("유효하지 않은 AccessToken 입니다.", e, INVALID_ACCESS_TOKEN);
        }
        return false;
    }

}

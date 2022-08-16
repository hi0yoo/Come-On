package com.comeon.authservice.auth.filter;

import com.comeon.authservice.auth.jwt.exception.*;
import com.comeon.authservice.auth.jwt.JwtTokenProvider;
import com.comeon.authservice.auth.jwt.JwtRepository;
import com.comeon.authservice.utils.CookieUtil;
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
        // AccessToken 존재하고, AccessToken 만료만 통과
        try {
            if (!StringUtils.hasText(accessToken)) {
                throw new JwtNotExistException("Access Token이 존재하지 않습니다.");
            }

            // TODO 예외 처리
            // AccessToken 블랙리스트 확인 -> 블랙리스트에 있으면 거부
            if (jwtRepository.findAccessToken(accessToken).isPresent()) {
                throw new JwtException("사용할 수 없는 Access Token 입니다.");
            }

            if (jwtTokenProvider.validate(accessToken)) {
                throw new AccessTokenNotExpiredException("만료되지 않은 Access Token은 재발급 할 수 없습니다.");
            }
        } catch (ExpiredJwtException e) {
            // AccessToken 만료 예외가 발생하면 RefreshToken 검증 시작
            // RefreshToken 검증에 실패하면 해당 필터를 통과하지 못한다.
            String refreshToken = CookieUtil.getCookie(request, CookieUtil.COOKIE_NAME_REFRESH_TOKEN)
                    .map(Cookie::getValue)
                    .orElseThrow(() -> new JwtNotExistException("Refresh Token이 존재하지 않습니다."));

            // Redis에 RT가 없으면, 전달받은 RT와 저장된 RT가 다르면, 유효하지 않은 리프레시 토큰
            refreshToken = jwtRepository.findRefreshTokenByUserId(jwtTokenProvider.getUserId(accessToken))
                    .filter(refreshToken::equals)
                    .orElseThrow(() -> new JwtException("재로그인 필요!!"));

            // RefreshToken 검증에 실패하면 예외 발생
            jwtTokenProvider.validate(refreshToken);
        } catch (JwtException e) {
            log.error("error", e);
            throw new InvalidAccessTokenException("유효하지 않은 Access Token 입니다.", e.getCause());
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

}

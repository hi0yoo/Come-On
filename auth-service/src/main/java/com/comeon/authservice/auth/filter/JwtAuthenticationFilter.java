package com.comeon.authservice.auth.filter;

import com.comeon.authservice.auth.jwt.exception.AccessTokenNotExistException;
import com.comeon.authservice.auth.jwt.exception.InvalidAccessTokenException;
import com.comeon.authservice.auth.jwt.exception.RefreshTokenNotExistException;
import com.comeon.authservice.auth.jwt.JwtTokenProvider;
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
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String accessToken = resolveAccessToken(request);
        // AccessToken 존재하고, 검증에 성공하거나 AccessToken 만료만 통과
        // AccessToken 검증시, 기한 만료 예외를 제외한 다른 예외가 발생하면 해당 필터를 통과하지 못한다.
        try {
            if (!StringUtils.hasText(accessToken)) {
                throw new AccessTokenNotExistException("Access Token이 존재하지 않습니다.");
            }
            jwtTokenProvider.validate(accessToken);
        } catch (ExpiredJwtException e) { // AccessToken 만료 예외는 잡는다.
            // AccessToken 만료 예외가 발생하면 RefreshToken 검증 시작
            // RefreshToken 검증에 실패하면 해당 필터를 통과하지 못한다.
            String refreshToken = CookieUtil.getCookie(request, CookieUtil.COOKIE_NAME_REFRESH_TOKEN)
                    .map(Cookie::getValue)
                    .orElseThrow(() -> new RefreshTokenNotExistException("Refresh Token이 존재하지 않습니다."));

            // RefreshToken 검증에 실패하면 예외 발생
            jwtTokenProvider.validate(refreshToken);
        } catch (JwtException e) {
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

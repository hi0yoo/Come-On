package com.comeon.authservice.auth.filter;

import com.comeon.authservice.auth.jwt.JwtTokenProvider;
import com.comeon.authservice.auth.jwt.exception.InvalidAccessTokenException;
import com.comeon.authservice.auth.jwt.JwtRepository;
import com.comeon.authservice.auth.jwt.exception.JwtNotExistException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRepository jwtRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String accessToken = resolveAccessToken(request);

        if (!StringUtils.hasText(accessToken)) {
            throw new JwtNotExistException("Access Token이 존재하지 않습니다.");
        }

        try {
            // TODO 검증 과정에 포함
            // AccessToken 블랙리스트 확인 -> 블랙리스트에 있으면 거부
            if (jwtRepository.findAccessToken(accessToken).isPresent()) {
                throw new JwtException("사용할 수 없는 Access Token 입니다.");
            }

            jwtTokenProvider.validate(accessToken);
        } catch (JwtException e) {
            throw new InvalidAccessTokenException("유효하지 않은 Access Token 입니다.", e.getCause());
        }

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

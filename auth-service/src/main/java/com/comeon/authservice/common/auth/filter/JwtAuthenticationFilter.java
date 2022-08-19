package com.comeon.authservice.common.auth.filter;

import com.comeon.authservice.common.auth.filter.exception.AuthorizationHeaderException;
import com.comeon.authservice.common.jwt.JwtTokenProvider;
import com.comeon.authservice.common.jwt.JwtRepository;
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
            throw new AuthorizationHeaderException();
        }

        if (jwtRepository.findBlackList(accessToken).isPresent()) {
            throw new JwtException("로그아웃 처리된 Access Token 입니다.");
        }

        jwtTokenProvider.validate(accessToken);

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

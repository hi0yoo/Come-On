package com.comeon.apigatewayservice.auth.filter;

import com.comeon.apigatewayservice.auth.jwt.JwtTokenProvider;
import com.comeon.apigatewayservice.common.exception.CustomException;
import com.comeon.apigatewayservice.common.exception.ErrorCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private static final String ROLE_KEY = "ROLE";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationGatewayFilterFactory(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList(ROLE_KEY);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 존재하는 bearer 타입의 authorization header가 없으면 null 반환
            String accessToken = resolveAccessToken(request);

            if (!(StringUtils.hasText(accessToken) && jwtTokenProvider.validate(accessToken))) {
                throw new CustomException("인증 헤더 검증에 실패하였습니다.", ErrorCode.INVALID_ACCESS_TOKEN);
            }

            String userRole = (String) jwtTokenProvider.getClaims(accessToken).get("auth");
            if (!hasRole(userRole, config.role)) {
                throw new CustomException("요청 수행에 대한 권한이 없습니다. 현재 권한 : " + userRole + ", 필요 권한 : " + config.role, ErrorCode.NO_PERMISSION);
            }

            return chain.filter(exchange);
        });
    }

    private boolean hasRole(String userRole, String requiredRole) {
        if (!StringUtils.hasText(userRole)) {
            return false;
        }

        if (!StringUtils.hasText(requiredRole)) {
            return true;
        }

        return userRole.equals(requiredRole);
    }

    private String resolveAccessToken(ServerHttpRequest request) {
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            throw new CustomException("인증 헤더를 찾을 수 없습니다.", ErrorCode.NO_AUTHORIZATION_HEADER);
        }

        List<String> authorizationHeaders = Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION));

        return authorizationHeaders.stream()
                .filter(token -> token.startsWith("Bearer "))
                .findFirst()
                .map(s -> s.substring(7))
                .orElse(null);
    }

    @Setter
    public static class Config {
        private String role;
    }
}

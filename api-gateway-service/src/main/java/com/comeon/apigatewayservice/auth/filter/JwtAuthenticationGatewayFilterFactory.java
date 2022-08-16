package com.comeon.apigatewayservice.auth.filter;

import com.comeon.apigatewayservice.auth.jwt.JwtTokenProvider;
import com.comeon.apigatewayservice.common.exception.NoPermissionException;
import com.comeon.apigatewayservice.common.exception.UnauthorizedException;
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

            String accessToken = resolveAccessToken(request);

            if (!(StringUtils.hasText(accessToken) && jwtTokenProvider.validate(accessToken))) {
                throw new UnauthorizedException();
            }

            String userRole = (String) jwtTokenProvider.getClaims(accessToken).get("auth");
            // 권한 학인
            if (!hasRole(userRole, config.role)) {
                throw new NoPermissionException("접근 권한이 없습니다. User role : " + userRole + ", Required role : " + config.role);
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
        String token = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    @Setter
    public static class Config {
        private String role;
    }
}

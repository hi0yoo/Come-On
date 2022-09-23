package com.comeon.apigatewayservice.auth.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Slf4j
@Component
public class GlobalLogFilter extends AbstractGatewayFilterFactory<GlobalLogFilter.Config> {

    public GlobalLogFilter() {
        super(Config.class);
    }

    @Getter
    @Setter
    public static class Config {
        private boolean preLogger;
        private boolean postLogger;
    }

    @Override
    public GatewayFilter apply(Config config) {

        // Pre Filter
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (config.isPreLogger()) {

                log.info("===== API Gateway Log Filter Start =====");
                log.info("*****        Request Start         *****");
                log.info("Request ID     : {}", request.getId());
                log.info("Request Address: {}", request.getRemoteAddress().getAddress().getHostAddress());
                log.info("Request Path   : {}", request.getPath().value());
                log.info("Request Method : {}", request.getMethodValue());
                log.info("Request Cookies: {}", request.getCookies());
                log.info("Request Headers: {}", request.getHeaders());
                log.info("*****         Request End          *****");
            }

            // Post Filter
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if (config.isPostLogger()) {

                    ServerHttpResponse response = exchange.getResponse();
                    log.info("*****        Response Start        *****");
                    log.info("Response Status : ", response.getStatusCode().value());
                    log.info("Response Header : {}", response.getHeaders());
                    log.info("*****         Response End         *****");
                    log.info("=====  API Gateway Log Filter End  =====");
                }
            }));
        };
    }

}
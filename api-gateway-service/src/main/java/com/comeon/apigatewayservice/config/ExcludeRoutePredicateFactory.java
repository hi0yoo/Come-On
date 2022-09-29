package com.comeon.apigatewayservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.function.Predicate;

import static org.springframework.http.server.PathContainer.parsePath;

@Slf4j
@Component
public class ExcludeRoutePredicateFactory extends AbstractRoutePredicateFactory<ExcludeRoutePredicateFactory.Config> {

    private final PathPatternParser pathPatternParser = new PathPatternParser();

    public ExcludeRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        log.info("[ExcludePredicate] path : {}, method : {}", config.getPattern(), config.getMethod());
        return exchange -> {
            HttpMethod requestMethod = exchange.getRequest().getMethod();
            PathContainer requestPath = parsePath(exchange.getRequest().getURI().getRawPath());

            PathPattern pathPattern = this.pathPatternParser.parse(config.getPattern());

            if (pathPattern.matches(requestPath) && requestMethod == config.getMethod()) {
                return false;
            }
            return true;
        };
    }

    public static class Config {
        private String pattern;
        private HttpMethod method;

        public String getPattern() {
            return pattern;
        }

        public Config setPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public void setMethod(HttpMethod method) {
            this.method = method;
        }
    }
}

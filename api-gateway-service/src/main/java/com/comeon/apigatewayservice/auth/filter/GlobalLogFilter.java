package com.comeon.apigatewayservice.auth.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
                log.info("Request Headers: {}", request.getHeaders());
                log.info("Request Cookies: {}", getRequestCookieString(request.getCookies()));
                log.info("Request Path   : {}", request.getPath().value());
                log.info("Request Method : {}", request.getMethodValue());
                String cachedRequestBodyObject = exchange.getAttribute("cachedRequestBodyObject");
                log.info("Request Body : {}", cachedRequestBodyObject);
                log.info("*****         Request End          *****");
            }

            // Post Filter
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if (config.isPostLogger()) {
                    ServerHttpResponse response = exchange.getResponse();

                    log.info("*****        Response Start        *****");
                    log.info("Response Status : ", response.getStatusCode());
                    log.info("Response Header : {}", response.getHeaders());
                    log.info("Response Cookies: {}", getResponseCookieString(response.getCookies()));
                    log.info("*****         Response End         *****");
                    log.info("=====  API Gateway Log Filter End  =====");
                }
            }));
        };
    }

    private String getBodyString(ServerHttpRequest request) {
        Flux<DataBuffer> body = request.getBody();

        AtomicReference<String> bodyRef = new AtomicReference<>();

        body.subscribe(buffer -> {

                    CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
                    DataBufferUtils.release(buffer);
                    bodyRef.set(charBuffer.toString());
                });
        String bodyString = bodyRef.get();
        return bodyString;
    }

    private String getRequestCookieString(MultiValueMap<String, HttpCookie> cookies) {
        return cookies.entrySet().stream()
                .map(entry -> {
                    List<HttpCookie> values = entry.getValue();
                    return entry.getKey() + ":" + (values.size() == 1 ?
                            "\"" + values.get(0) + "\"" :
                            values.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));
                })
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String getResponseCookieString(MultiValueMap<String, ResponseCookie> cookies) {
        return cookies.entrySet().stream()
                .map(entry -> {
                    List<ResponseCookie> values = entry.getValue();
                    return entry.getKey() + ":" + (values.size() == 1 ?
                            "\"" + values.get(0) + "\"" :
                            values.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));
                })
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
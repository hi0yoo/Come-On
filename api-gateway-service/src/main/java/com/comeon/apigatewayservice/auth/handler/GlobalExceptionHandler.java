package com.comeon.apigatewayservice.auth.handler;

import com.comeon.apigatewayservice.common.response.ApiResponse;
import com.comeon.apigatewayservice.common.response.ErrorCode;
import com.comeon.apigatewayservice.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.Hints;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("인증 예외 발생", ex);

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorCode errorCode = ErrorCode.createErrorCode(ex);
        response.setStatusCode(errorCode.getHttpStatus());

        // TODO 리팩토링
        ApiResponse<ErrorResponse> errorResponse = null;
        if (errorCode.getHttpStatus().equals(HttpStatus.FORBIDDEN)) {
            errorResponse = ApiResponse.createForbidden(errorCode);
        } else {
            errorResponse = ApiResponse.createUnauthorized(errorCode);
        }

        return response.writeWith(
                new Jackson2JsonEncoder(objectMapper).encode(
                        Mono.just(errorResponse),
                        response.bufferFactory(),
                        ResolvableType.forInstance(errorResponse),
                        MediaType.APPLICATION_JSON,
                        Hints.from(Hints.LOG_PREFIX_HINT, exchange.getLogPrefix())
                )
        );
    }
}

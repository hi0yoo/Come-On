package com.comeon.apigatewayservice.auth.handler;

import com.comeon.apigatewayservice.common.exception.CustomException;
import com.comeon.apigatewayservice.common.exception.ErrorCode;
import com.comeon.apigatewayservice.common.response.ApiResponse;
import com.comeon.apigatewayservice.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.Hints;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
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
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = exchange.getRequest().getHeaders();
        response.getHeaders().setContentType(headers.getContentType());

        ErrorCode errorCode;

        if (ex.getClass().equals(CustomException.class)) {
            log.error("[{}] {}",
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    ex
            );
            CustomException customException = (CustomException) ex;
            errorCode = customException.getErrorCode();
        } else if (ex.getClass().equals(ResponseStatusException.class)) {
            log.error("[{}] {}, requestPath : {}",
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    exchange.getRequest().getPath(),
                    ex
            );
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;

            errorCode = ErrorCode.byResponseStatusExceptionStatus(responseStatusException.getStatus());
        } else {
            log.error("[{}] {}",
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    ex
            );
            errorCode = ErrorCode.SERVER_ERROR;
        }

        ApiResponse<ErrorResponse> apiResponse = ApiResponse.createError(errorCode);

        return response.writeWith(
                new Jackson2JsonEncoder(objectMapper).encode(
                        Mono.just(apiResponse),
                        response.bufferFactory(),
                        ResolvableType.forInstance(apiResponse),
                        MediaType.APPLICATION_JSON,
                        Hints.from(Hints.LOG_PREFIX_HINT, exchange.getLogPrefix())
                )
        );
    }
}


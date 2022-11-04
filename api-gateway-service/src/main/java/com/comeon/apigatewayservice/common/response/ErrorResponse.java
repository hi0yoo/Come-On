package com.comeon.apigatewayservice.common.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse<T> {

    private Integer errorCode;
    private T message;
}

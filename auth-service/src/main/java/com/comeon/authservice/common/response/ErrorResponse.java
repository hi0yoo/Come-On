package com.comeon.authservice.common.response;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse<T> {

    private Integer code;
    private T message;

}

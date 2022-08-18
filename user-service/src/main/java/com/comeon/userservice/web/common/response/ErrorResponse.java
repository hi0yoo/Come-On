package com.comeon.userservice.web.common.response;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {

    private Integer code;
    private String message;

}

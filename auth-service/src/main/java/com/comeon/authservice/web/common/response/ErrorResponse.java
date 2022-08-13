package com.comeon.authservice.web.common.response;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {

    private String code;
    private String message;

}

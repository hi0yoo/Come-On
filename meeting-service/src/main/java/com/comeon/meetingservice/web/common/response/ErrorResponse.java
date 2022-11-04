package com.comeon.meetingservice.web.common.response;

import lombok.*;

import static lombok.AccessLevel.*;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ErrorResponse<T> {

    private Integer errorCode;
    private T message;

}

package com.comeon.meetingservice.web.common.feign.userservice;

import lombok.*;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class UserServiceApiResponse<T> {

    private LocalDateTime responseTime;
    private String code;
    private T data;

}

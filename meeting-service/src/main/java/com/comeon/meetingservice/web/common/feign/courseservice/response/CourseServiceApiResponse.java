package com.comeon.meetingservice.web.common.feign.courseservice.response;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class CourseServiceApiResponse<T> {

    private String responseTime;
    private String code;
    private T data;

}

package com.comeon.meetingservice.web.common.feign.courseservice.response;

import lombok.*;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class CourseServiceListResponse<T> {

    private Integer count;
    private List<T> contents;

}

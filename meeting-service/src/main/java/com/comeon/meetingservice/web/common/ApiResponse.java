package com.comeon.meetingservice.web.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Collection;

import static lombok.AccessLevel.*;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private static final String SUCCESS_STATUS = "success";
    private static final String ERROR_STATUS = "error";

    private LocalDateTime timeStamp;

    private String status;

    private Integer count;

    private T contents;

    private ErrorResponse error;

    public static <T> ApiResponse<T> createDetail(T contents) {
        return ApiResponse.<T>builder()
                .timeStamp(LocalDateTime.now())
                .status(SUCCESS_STATUS)
                .contents(contents)
                .build();
    }

    public static <T extends Collection> ApiResponse<T> createList(T contents) {
        return ApiResponse.<T>builder()
                .timeStamp(LocalDateTime.now())
                .status(SUCCESS_STATUS)
                .count(contents.size())
                .contents(contents)
                .build();
    }

    public static ApiResponse<?> createError(ErrorResponse errorResponse) {
        return ApiResponse.builder()
                .timeStamp(LocalDateTime.now())
                .status(ERROR_STATUS)
                .error(errorResponse)
                .build();
    }


}

package com.comeon.meetingservice.web.restdocs.docscontroller;

import com.comeon.meetingservice.web.common.response.ApiResponse;
import com.comeon.meetingservice.web.common.response.ApiResponseCode;
import com.comeon.meetingservice.web.common.response.EnumType;
import com.comeon.meetingservice.web.common.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
public class DocsController {


    @GetMapping("/docs")
    public ApiResponse<Docs> docs() {
        Map<String, String> apiResponseCodes = getDocs(ApiResponseCode.values());

        return ApiResponse.createSuccess(
                Docs.builder()
                        .apiResponseCodes(apiResponseCodes)
                        .build()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @GetMapping("/docs/error")
    public ApiResponse<ErrorResponse> docsError() {
        return ApiResponse.createBadParameter("101", "error message");
    }

    private Map<String, String> getDocs(EnumType[] enumTypes) {
        return Arrays.stream(enumTypes)
                .collect(Collectors.toMap(EnumType::getId, EnumType::getText));
    }

}

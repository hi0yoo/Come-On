package com.comeon.meetingservice.web.restdocs.docscontroller;

import com.comeon.meetingservice.domain.common.exception.EntityNotFoundException;
import com.comeon.meetingservice.web.common.response.*;
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
        Map<Integer, String> errorCodes = Arrays.stream(ErrorCode.values())
                .collect(Collectors.toMap(ErrorCode::getCode, ErrorCode::getInstruction));

        return ApiResponse.createSuccess(
                Docs.builder()
                        .apiResponseCodes(apiResponseCodes)
                        .errorCodes(errorCodes)
                        .build()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @GetMapping("/docs/error")
    public ApiResponse<ErrorResponse> docsError() {
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException("Error Message");
        return ApiResponse.createBadParameter(entityNotFoundException);
    }

    private Map<String, String> getDocs(EnumType[] enumTypes) {
        return Arrays.stream(enumTypes)
                .collect(Collectors.toMap(EnumType::getId, EnumType::getText));
    }



}

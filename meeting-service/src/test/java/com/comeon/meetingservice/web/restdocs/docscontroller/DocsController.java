package com.comeon.meetingservice.web.restdocs.docscontroller;

import com.comeon.meetingservice.domain.common.exception.EntityNotFoundException;
import com.comeon.meetingservice.web.common.response.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
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

    @GetMapping("/docs/list")
    public ApiResponse<SliceResponse<Map<String, String>>> docsList() {
        List<Map<String, String>> demoList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Map<String, String> demoContent = new HashMap<>();
            demoContent.put("id", String.valueOf(i + 1));
            demoContent.put("name", "sample" + (i + 1));
            demoList.add(demoContent);
        }

        SliceResponse<Map<String, String>> demoSlice = SliceResponse.<Map<String, String>>builder()
                .currentSlice(0)
                .sizePerSlice(5)
                .numberOfElements(3)
                .hasPrevious(false)
                .hasNext(false)
                .isFirst(true)
                .isLast(false)
                .contents(demoList)
                .build();

        return ApiResponse.createSuccess(demoSlice);
    }

    private Map<String, String> getDocs(EnumType[] enumTypes) {
        return Arrays.stream(enumTypes)
                .collect(Collectors.toMap(EnumType::getId, EnumType::getText));
    }



}

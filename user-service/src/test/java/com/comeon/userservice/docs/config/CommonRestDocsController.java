package com.comeon.userservice.docs.config;

import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.UserRole;
import com.comeon.userservice.domain.user.entity.UserStatus;
import com.comeon.userservice.web.common.response.ApiResponse;
import com.comeon.userservice.web.common.response.ApiResponseCode;
import com.comeon.userservice.web.common.response.ErrorResponse;
import com.comeon.userservice.web.common.response.ListResponse;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@ActiveProfiles("test")
public class CommonRestDocsController {

    @GetMapping("/docs/success")
    public ApiResponse<?> commonResponseFields() {
        Map<String, String> responseCodes = Arrays.stream(ApiResponseCode.values())
                .collect(Collectors.toMap(ApiResponseCode::getId, ApiResponseCode::getText));

        return ApiResponse.createSuccess(responseCodes);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @GetMapping("/docs/error")
    public ApiResponse<ErrorResponse> commonErrorResponseFields() {
        return ApiResponse.createServerError(ErrorCode.SERVER_ERROR);
    }

    @GetMapping("/docs/list/response")
    public ApiResponse<ListResponse> listResponseFormat() {
        return ApiResponse.createSuccess(
                ListResponse.toListResponse(List.of(1, 2, 3))
        );
    }

    @GetMapping("/docs/error/codes")
    public ApiResponse<?> errorResponseCodes() {
        Map<Integer, String> errorCodes = Arrays.stream(ErrorCode.values())
                .collect(Collectors.toMap(ErrorCode::getCode, ErrorCode::getMessage));
        return ApiResponse.createSuccess(errorCodes);
    }

    @GetMapping("/docs/providers")
    public ApiResponse<?> providerCods() {
        Map<String, String> providers = Arrays.stream(OAuthProvider.values())
                .collect(Collectors.toMap(OAuthProvider::getValue, OAuthProvider::getDescription));
        return ApiResponse.createSuccess(providers);
    }

    @GetMapping("/docs/user-status")
    public ApiResponse<?> userStatusCodes() {
        Map<String, String> userStatusMap = Arrays.stream(UserStatus.values())
                .collect(Collectors.toMap(UserStatus::name, UserStatus::getDescription));
        return ApiResponse.createSuccess(userStatusMap);
    }

    @GetMapping("/docs/user-role")
    public ApiResponse<?> userRoleCodes() {
        Map<String, String> userRoleMap = Arrays.stream(UserRole.values())
                .collect(Collectors.toMap(UserRole::getRoleValue, UserRole::getDescription));
        return ApiResponse.createSuccess(userRoleMap);
    }
}

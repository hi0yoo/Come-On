package com.comeon.userservice.web.feign.authservice;

import com.comeon.userservice.web.feign.authservice.response.LogoutSuccessResponse;
import com.comeon.userservice.web.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface AuthServiceFeignClient {

    @PostMapping("/auth/logout")
    ApiResponse<LogoutSuccessResponse> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken);
}

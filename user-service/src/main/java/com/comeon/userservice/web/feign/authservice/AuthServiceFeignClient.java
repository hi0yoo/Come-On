package com.comeon.userservice.web.feign.authservice;

import com.comeon.userservice.web.feign.authservice.response.UnlinkResponse;
import com.comeon.userservice.web.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service")
public interface AuthServiceFeignClient {

    @PostMapping("/auth/unlink")
    ApiResponse<UnlinkResponse> unlink(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerAccessToken,
            @RequestParam Long userOauthId);
}

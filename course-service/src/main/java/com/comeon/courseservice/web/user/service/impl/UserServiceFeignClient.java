package com.comeon.courseservice.web.user.service.impl;

import com.comeon.courseservice.web.common.response.ApiResponse;
import com.comeon.courseservice.web.user.service.UserService;
import com.comeon.courseservice.web.user.service.response.UserDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceFeignClient extends UserService {

    @GetMapping("/users/{userId}")
    ApiResponse<UserDetailsResponse> getUserDetails(@PathVariable Long userId);
}

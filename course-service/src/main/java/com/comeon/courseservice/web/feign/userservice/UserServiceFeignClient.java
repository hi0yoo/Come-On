package com.comeon.courseservice.web.feign.userservice;

import com.comeon.courseservice.web.common.response.ApiResponse;
import com.comeon.courseservice.web.feign.userservice.response.ListResponse;
import com.comeon.courseservice.web.feign.userservice.response.UserDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserServiceFeignClient {

    @GetMapping("/users/{userId}")
    ApiResponse<UserDetailsResponse> getUserDetails(@PathVariable Long userId);

    @GetMapping("/users")
    ApiResponse<ListResponse<UserDetailsResponse>> userList(@RequestParam List<Long> userIds);
}

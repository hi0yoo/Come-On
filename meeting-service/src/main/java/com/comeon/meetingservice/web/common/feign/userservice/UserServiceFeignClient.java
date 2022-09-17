package com.comeon.meetingservice.web.common.feign.userservice;

import com.comeon.meetingservice.web.common.feign.userservice.response.UserDetailResponse;
import com.comeon.meetingservice.web.common.feign.userservice.response.UserListResponse;
import com.comeon.meetingservice.web.common.feign.userservice.response.UserServiceApiResponse;
import com.comeon.meetingservice.web.common.feign.userservice.response.UserServiceListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@FeignClient(name = "user-service")
public interface UserServiceFeignClient {

    @GetMapping("/users")
    UserServiceApiResponse<UserServiceListResponse<UserListResponse>> getUsers(
            @RequestParam("userIds") Set<Long> userIds);

    @GetMapping("/users/{userId}")
    UserServiceApiResponse<UserDetailResponse> getUser(@PathVariable("userId") Long userId);

}

package com.comeon.authservice.feign.userservice;

import com.comeon.authservice.feign.userservice.request.UserSaveRequest;
import com.comeon.authservice.feign.userservice.response.UserSaveResponse;
import com.comeon.authservice.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@FeignClient(name = "user-service")
public interface UserServiceFeignClient {

    @PostMapping("/users")
    ApiResponse<UserSaveResponse> saveUser(@RequestBody UserSaveRequest request);



    // for test
    @PostMapping("/user-test-api/users/init")
    List<Long> initUsers();
}

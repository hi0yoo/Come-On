package com.comeon.courseservice.config.custommock;

import com.comeon.courseservice.web.common.response.ApiResponse;
import com.comeon.courseservice.web.user.service.UserService;
import com.comeon.courseservice.web.user.service.response.UserDetailsResponse;

import java.util.ArrayList;
import java.util.List;

public class MockUserServiceFeignClient implements UserService {

    @Override
    public ApiResponse<UserDetailsResponse> getUserDetails(Long userId) {
        List<Long> userIdList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            userIdList.add(Integer.valueOf(i).longValue());
        }

        if (userIdList.contains(userId)) {
            String profileImgUrl = "http://127.0.0.1:8001/comeon-file/user-dev/ff22a4d3-4f47-4e7a-be5b-37c65bb5985c.jpeg";

            return ApiResponse.createSuccess(
                    new UserDetailsResponse(
                            userId,
                            "userNickname",
                            profileImgUrl)
            );
        }

        return null;
    }
}

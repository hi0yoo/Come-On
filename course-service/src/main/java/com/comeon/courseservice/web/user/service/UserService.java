package com.comeon.courseservice.web.user.service;

import com.comeon.courseservice.web.common.response.ApiResponse;
import com.comeon.courseservice.web.user.service.response.UserDetailsResponse;

public interface UserService {

    ApiResponse<UserDetailsResponse> getUserDetails(Long userId);
}

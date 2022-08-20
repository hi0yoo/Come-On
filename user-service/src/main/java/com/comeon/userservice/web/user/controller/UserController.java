package com.comeon.userservice.web.user.controller;

import com.comeon.userservice.common.argresolver.CurrentUserId;
import com.comeon.userservice.domain.user.dto.UserDto;
import com.comeon.userservice.domain.user.service.UserService;
import com.comeon.userservice.web.common.exception.ValidateException;
import com.comeon.userservice.web.common.response.ApiResponse;
import com.comeon.userservice.web.user.request.UserSaveRequest;
import com.comeon.userservice.web.user.response.UserDetailResponse;
import com.comeon.userservice.web.user.response.UserSaveResponse;
import com.comeon.userservice.web.user.response.MyDetailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ApiResponse<UserSaveResponse> userSave(@Validated @RequestBody UserSaveRequest request,
                                                  BindingResult bindingResult) {
        // TODO 예외 처리
        if (bindingResult.hasErrors()) {
            throw new ValidateException(bindingResult);
        }

        return ApiResponse.createSuccess(
                new UserSaveResponse(userService.saveUser(request.toServiceDto()))
        );
    }

    // 회원 정보 조회
    @GetMapping("/{userId}")
    public ApiResponse<UserDetailResponse> userDetails(@PathVariable Long userId) {
        UserDto userDto = userService.findUser(userId);

        return ApiResponse.createSuccess(new UserDetailResponse(userDto));
    }

    // 내 상세정보 조회
    @GetMapping("/me")
    public ApiResponse<MyDetailsResponse> myDetails(@CurrentUserId Long currentUserId) {
        UserDto userDto = userService.findUser(currentUserId);

        return ApiResponse.createSuccess(new MyDetailsResponse(userDto));
    }

    // TODO 유저 정보 수정

    // TODO 회원 탈퇴

}

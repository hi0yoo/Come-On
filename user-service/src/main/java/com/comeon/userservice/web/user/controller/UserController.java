package com.comeon.userservice.web.user.controller;

import com.comeon.userservice.domain.user.dto.UserDto;
import com.comeon.userservice.domain.user.service.UserService;
import com.comeon.userservice.web.common.exception.ValidateException;
import com.comeon.userservice.web.common.response.ApiResponse;
import com.comeon.userservice.web.user.request.UserSaveRequest;
import com.comeon.userservice.web.user.response.UserSaveResponse;
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

        UserDto savedUserDto = userService.saveUser(request.toServiceDto());

        return ApiResponse.createSuccess(
                new UserSaveResponse(
                        savedUserDto.getId(),
                        savedUserDto.getRole().getRoleValue()
                )
        );
    }

    // TODO 내 정보 조회

    // TODO 회원 정보 조회
    // 닉네임, 프로필 이미지

    // TODO 회원 정보 수정

    // TODO 회원 탈퇴
}

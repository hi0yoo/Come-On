package com.comeon.userservice.web.user.controller;

import com.comeon.userservice.config.argresolver.CurrentUserId;
import com.comeon.userservice.domain.user.service.UserService;
import com.comeon.userservice.web.feign.authservice.AuthServiceFeignClient;
import com.comeon.userservice.web.common.aop.ValidationRequired;
import com.comeon.userservice.web.common.response.ApiResponse;
import com.comeon.userservice.web.user.query.UserQueryService;
import com.comeon.userservice.web.user.request.UserModifyRequest;
import com.comeon.userservice.web.user.request.UserSaveRequest;
import com.comeon.userservice.web.user.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final AuthServiceFeignClient authServiceFeignClient;

    private final UserService userService;
    private final UserQueryService userQueryService;

    // 회원 정보 저장
    @PostMapping
    @ValidationRequired
    public ApiResponse<UserDetailResponse> userSave(@Validated @RequestBody UserSaveRequest request,
                                                    BindingResult bindingResult) {
        Long userId = userService.saveUser(request.toServiceDto());

        return ApiResponse.createSuccess(userQueryService.getUserDetails(userId));
    }

    // 회원 정보 조회
    @GetMapping("/{userId}")
    public ApiResponse<UserSimpleResponse> userDetails(@PathVariable Long userId) {
        return ApiResponse.createSuccess(userQueryService.getUserSimple(userId));
    }

    // 내 상세정보 조회
    @GetMapping("/me")
    public ApiResponse<UserDetailResponse> myDetails(@CurrentUserId Long currentUserId) {
        return ApiResponse.createSuccess(userQueryService.getUserDetails(currentUserId));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ApiResponse<UserWithdrawResponse> userWithdraw(@CurrentUserId Long currentUserId,
                                                          HttpServletRequest httpServletRequest) {
        userService.withdrawUser(currentUserId);

        String bearerAccessToken = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        // TODO 실패하면 오류 응답인데 어떻게 처리?
        authServiceFeignClient.logout(bearerAccessToken);

        return ApiResponse.createSuccess(new UserWithdrawResponse());
    }

    // 유저 정보 수정
    @ValidationRequired
    @PatchMapping("/me")
    public ApiResponse<?> userModify(@CurrentUserId Long currentUserId,
                                     @Validated @RequestBody UserModifyRequest request,
                                     BindingResult bindingResult) {
        userService.modifyUser(currentUserId, request.toServiceDto());

        return ApiResponse.createSuccess(new UserModifyResponse());
    }

}

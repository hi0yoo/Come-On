package com.comeon.userservice.web.user.controller;

import com.comeon.userservice.config.argresolver.CurrentUserId;
import com.comeon.userservice.domain.user.service.UserService;
import com.comeon.userservice.web.common.response.ListResponse;
import com.comeon.userservice.web.feign.authservice.AuthFeignService;
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
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final AuthFeignService authFeignService;

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

    // 회원 리스트 조회
    @GetMapping
    public ApiResponse<ListResponse<UserSimpleResponse>> userList(@RequestParam List<Long> userIds) {
        return ApiResponse.createSuccess(
                userQueryService.getUserList(userIds)
        );
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
        // auth-service에 회원 탈퇴 요청
        Long userOauthId = userQueryService.getUserOauthId(currentUserId);
        String accessToken = resolveAccessToken(httpServletRequest);
        authFeignService.userUnlink(accessToken, userOauthId);

        userService.withdrawUser(currentUserId);

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


    private String resolveAccessToken(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION).substring(7);
    }
}

package com.comeon.userservice.web.user.controller;

import com.comeon.userservice.common.argresolver.CurrentUserId;
import com.comeon.userservice.domain.user.dto.ProfileImgDto;
import com.comeon.userservice.domain.user.dto.UserDto;
import com.comeon.userservice.domain.user.service.UserService;
import com.comeon.userservice.web.common.exception.ValidateException;
import com.comeon.userservice.web.common.file.FileManager;
import com.comeon.userservice.web.common.file.UploadFileDto;
import com.comeon.userservice.web.common.response.ApiResponse;
import com.comeon.userservice.web.user.request.UserModifyRequest;
import com.comeon.userservice.web.user.request.UserSaveRequest;
import com.comeon.userservice.web.user.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    @Value("${profile.dirName}")
    private String dirName;

    private final UserService userService;
    private final FileManager fileManager;

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
    public ApiResponse<UserSimpleResponse> userDetails(@PathVariable Long userId) {
        UserDto userDto = userService.findUser(userId);

        String fileUrl = null; // TODO 지저분하다.. 리팩토링 필요
        if (userDto.getProfileImgDto() != null) {
            fileUrl = fileManager.getFileUrl(dirName, userDto.getProfileImgDto().getStoredName());
        }

        return ApiResponse.createSuccess(new UserSimpleResponse(userDto, fileUrl));
    }

    // 내 상세정보 조회
    @GetMapping("/me")
    public ApiResponse<UserDetailResponse> myDetails(@CurrentUserId Long currentUserId) {
        UserDto userDto = userService.findUser(currentUserId);

        String fileUrl = null; // TODO 지저분하다.. 리팩토링 필요
        if (userDto.getProfileImgDto() != null) {
            fileUrl = fileManager.getFileUrl(dirName, userDto.getProfileImgDto().getStoredName());
        }

        return ApiResponse.createSuccess(new UserDetailResponse(userDto, fileUrl));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ApiResponse<UserWithdrawResponse> userWithdraw(@CurrentUserId Long currentUserId) {
        userService.withdrawUser(currentUserId);

        // TODO Auth-Service에 로그아웃 요청

        return ApiResponse.createSuccess(new UserWithdrawResponse());
    }

    // 유저 정보 수정
    @PatchMapping("/me")
    public ApiResponse<?> userModify(@CurrentUserId Long currentUserId,
                                     @RequestBody UserModifyRequest request) {
        userService.modifyUser(currentUserId, request.toServiceDto());

        return ApiResponse.createSuccess();
    }

    // 유저 프로필 이미지 수정
    @PostMapping("/me/image")
    public ApiResponse<UserImageSaveResponse> userImageSave(@CurrentUserId Long currentUserId,
                                                            MultipartFile imgFile) {
        // 기존 이미지를 지워야 하다보니, 이를 위한 조회 쿼리 추가되었음. 다른 방법이 없을까..
        String storedFileName = userService.findUser(currentUserId).getProfileImgDto().getStoredName();
        fileManager.delete(storedFileName, dirName);

        UploadFileDto uploadFile = fileManager.upload(imgFile, dirName);
        ProfileImgDto profileImgDto = ProfileImgDto.builder()
                .originalName(uploadFile.getOriginalFileName())
                .storedName(uploadFile.getStoredFileName())
                .build();

        userService.modifyProfileImg(currentUserId, profileImgDto);

        String imgUrl = fileManager.getFileUrl(dirName, profileImgDto.getStoredName());

        return ApiResponse.createSuccess(new UserImageSaveResponse(imgUrl));
    }

    // 유저 프로필이미지 삭제
    @DeleteMapping("/me/image")
    public ApiResponse<?> userImageRemove(@CurrentUserId Long currentUserId) {
        String storedFilenameOfRemoved = userService.removeProfileImg(currentUserId);

        fileManager.delete(storedFilenameOfRemoved, dirName);

        return ApiResponse.createSuccess();
    }

}

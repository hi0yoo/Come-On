package com.comeon.userservice.web.profileimage.controller;

import com.comeon.userservice.config.argresolver.CurrentUserId;
import com.comeon.userservice.domain.profileimage.service.dto.ProfileImgDto;
import com.comeon.userservice.domain.profileimage.service.ProfileImgService;
import com.comeon.userservice.web.common.aop.ValidationRequired;
import com.comeon.userservice.web.common.file.FileManager;
import com.comeon.userservice.web.common.file.UploadedFileInfo;
import com.comeon.userservice.web.common.response.ApiResponse;
import com.comeon.userservice.web.profileimage.query.ProfileImgQueryService;
import com.comeon.userservice.web.profileimage.request.ProfileImgSaveRequest;
import com.comeon.userservice.web.profileimage.response.ProfileImgRemoveResponse;
import com.comeon.userservice.web.profileimage.response.ProfileImgSaveResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile-image")
public class ProfileImgController {

    @Value("${s3.folder-name.user}")
    private String dirName;

    private final FileManager fileManager;
    private final ProfileImgService profileImgService;
    private final ProfileImgQueryService profileImgQueryService;

    //유저 프로필 이미지 수정
    @ValidationRequired
    @PostMapping
    public ApiResponse<ProfileImgSaveResponse> profileImageSave(@CurrentUserId Long currentUserId,
                                                                @Validated @ModelAttribute ProfileImgSaveRequest request,
                                                                BindingResult bindingResult) {
        ProfileImgDto profileImgDto = generateProfileImgDto(
                fileManager.upload(request.getImgFile(), dirName)
        );

        Long profileImgId = null;
        String fileNameToDelete = profileImgQueryService.getStoredFileNameByUserId(currentUserId);
        try {
            profileImgId = profileImgService.saveProfileImg(profileImgDto, currentUserId);
        } catch (RuntimeException e) {
            fileNameToDelete = profileImgDto.getStoredName();
            throw e;
        } finally {
            if (fileNameToDelete != null) {
                fileManager.delete(fileNameToDelete, dirName);
            }
        }

        String imgUrl = fileManager.getFileUrl(profileImgDto.getStoredName(), dirName);

        return ApiResponse.createSuccess(new ProfileImgSaveResponse(profileImgId, imgUrl));
    }

    // 유저 프로필이미지 삭제
    @DeleteMapping("/{profileImgId}")
    public ApiResponse<?> profileImageRemove(@CurrentUserId Long currentUserId,
                                             @PathVariable Long profileImgId) {
        String storedFilenameToRemove =
                profileImgQueryService.getStoredFileNameByProfileImgIdAndUserId(profileImgId, currentUserId);

        profileImgService.removeProfileImg(profileImgId);

        fileManager.delete(storedFilenameToRemove, dirName);

        return ApiResponse.createSuccess(new ProfileImgRemoveResponse());
    }


    /* ### private method ### */
    private ProfileImgDto generateProfileImgDto(UploadedFileInfo uploadedFileInfo) {
        return ProfileImgDto.builder()
                .originalName(uploadedFileInfo.getOriginalFileName())
                .storedName(uploadedFileInfo.getStoredFileName())
                .build();
    }
}

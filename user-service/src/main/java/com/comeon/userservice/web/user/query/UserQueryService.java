package com.comeon.userservice.web.user.query;

import com.comeon.userservice.common.exception.CustomException;
import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.web.common.file.FileManager;
import com.comeon.userservice.web.common.response.ListResponse;
import com.comeon.userservice.web.user.response.UserDetailResponse;
import com.comeon.userservice.web.user.response.UserSimpleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    @Value("${s3.folder-name.user}")
    private String dirName;

    private final UserQueryRepository userQueryRepository;
    private final FileManager fileManager;

    public UserDetailResponse getUserDetails(Long userId) {
        User user = getUser(userId);
        String fileUrl = getFileUrl(user);

        return new UserDetailResponse(user, fileUrl);
    }

    public UserSimpleResponse getUserSimple(Long userId) {
        User user = getUser(userId);
        String fileUrl = getFileUrl(user);

        return new UserSimpleResponse(user, fileUrl);
    }

    public ListResponse<UserSimpleResponse> getUserList(List<Long> userIds) {
        return ListResponse.toListResponse(
                userQueryRepository.findByIdInIdListFetchProfileImg(userIds).stream()
                        .map(user -> {
                                    if (user.isActivateUser()) {
                                        return UserSimpleResponse.activateUserResponseBuilder()
                                                .user(user)
                                                .profileImgUrl(getFileUrl(user))
                                                .build();
                                    }
                                    return UserSimpleResponse.withdrawnUserResponseBuilder()
                                            .user(user)
                                            .build();
                                }
                        ).collect(Collectors.toList())
        );
    }


    /* ### private method ### */
    // TODO 수정
    private User getUser(Long userId) {
        User user = userQueryRepository.findByIdFetchAll(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별자를 가진 User가 없습니다. 요청한 User 식별값 : " + userId)
                );

        if (!user.isActivateUser()) {
            throw new CustomException("탈퇴한 회원입니다. 요청한 User 식별값 : " + userId, ErrorCode.ALREADY_WITHDRAW);
        }

        return user;
    }

    private String getFileUrl(User user) {
        if (user.getProfileImg() != null) {
            return fileManager.getFileUrl(user.getProfileImg().getStoredName(), dirName);
        }
        return null;
    }
}

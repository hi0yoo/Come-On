package com.comeon.userservice.web.user.query;

import com.comeon.userservice.common.exception.CustomException;
import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.web.common.file.FileManager;
import com.comeon.userservice.web.user.response.UserDetailResponse;
import com.comeon.userservice.web.user.response.UserSimpleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    @Value("${profile.dirName}")
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


    /* ### private method ### */
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
            return fileManager.getFileUrl(dirName, user.getProfileImg().getStoredName());
        }
        return null;
    }
}

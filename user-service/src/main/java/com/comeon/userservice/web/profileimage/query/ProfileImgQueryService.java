package com.comeon.userservice.web.profileimage.query;

import com.comeon.userservice.common.exception.CustomException;
import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
import com.comeon.userservice.domain.profileimage.entity.ProfileImg;
import com.comeon.userservice.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileImgQueryService {

    private final ProfileImgQueryRepository profileImgQueryRepository;

    public String getStoredFileNameByUserId(Long userId) {
        ProfileImg profileImg = profileImgQueryRepository.findByUserId(userId).orElse(null);

        if (profileImg == null) {
            return null;
        }

        isActivateUser(profileImg.getUser());

        return profileImg.getStoredName();
    }

    public String getStoredFileNameByProfileImgIdAndUserId(Long profileImgId, Long userId) {
        ProfileImg profileImg = profileImgQueryRepository.findById(profileImgId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별자를 가진 ProfileImg가 없습니다. 요청한 ProfileImg 식별값 : " + profileImgId)
                );

        isActivateUser(profileImg.getUser());

        if (!Objects.equals(profileImg.getUser().getId(), userId)) {
            throw new CustomException("요청을 수행 할 권한이 없습니다. 요청한 User 식별값 : " + userId, ErrorCode.NO_AUTHORITIES);
        }

        return profileImg.getStoredName();
    }


    /* ### private method ### */
    private void isActivateUser(User user) {
        if (!user.isActivateUser()) {
            throw new CustomException("탈퇴한 회원입니다. 요청한 User 식별값 : " + user.getId(), ErrorCode.ALREADY_WITHDRAW);
        }
    }
}

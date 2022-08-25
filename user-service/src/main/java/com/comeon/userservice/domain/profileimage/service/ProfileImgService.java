package com.comeon.userservice.domain.profileimage.service;

import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
import com.comeon.userservice.domain.profileimage.service.dto.ProfileImgDto;
import com.comeon.userservice.domain.profileimage.entity.ProfileImg;
import com.comeon.userservice.domain.profileimage.repository.ProfileImgRepository;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProfileImgService {

    private final ProfileImgRepository profileImgRepository;
    private final UserRepository userRepository;

    public Long saveProfileImg(ProfileImgDto profileImgDto, Long userId) {
        ProfileImg profileImg = profileImgRepository.findByUserId(userId).orElse(null);

        if (profileImg == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow();
            profileImg = profileImgRepository.save(
                    ProfileImg.builder()
                            .user(user)
                            .originalName(profileImgDto.getOriginalName())
                            .storedName(profileImgDto.getStoredName())
                            .build()
            );
        } else {
            profileImg.updateOriginalName(profileImgDto.getOriginalName());
            profileImg.updateStoredName(profileImgDto.getStoredName());
        }

        return profileImg.getId();
    }

    public void removeProfileImg(Long profileImgId) {
        ProfileImg profileImg = profileImgRepository.findById(profileImgId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별자를 가진 ProfileImg가 없습니다. 요청한 ProfileImg 식별값 : " + profileImgId)
                );
        profileImgRepository.delete(profileImg);
    }
}

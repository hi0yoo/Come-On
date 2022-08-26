package com.comeon.userservice.domain.user.service;

import com.comeon.userservice.common.exception.CustomException;
import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.repository.UserRepository;
import com.comeon.userservice.domain.user.service.dto.ModifyUserInfoFields;
import com.comeon.userservice.domain.user.service.dto.UserAccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Long saveUser(UserAccountDto accountDto) {
        Optional<User> findUser = userRepository.findByOAuthIdAndProvider(
                accountDto.getOauthId(),
                accountDto.getProvider()
        );

        User user = null;
        if (findUser.isPresent()) {
            user = findUser.orElseThrow();
            user.getAccount().updateOAuthInfo(
                    accountDto.getEmail(),
                    accountDto.getName(),
                    accountDto.getProfileImgUrl()
            );
        } else {
            User signupUser = User.builder()
                    .account(accountDto.toEntity())
                    .build();
            user = userRepository.save(signupUser);
        }

        return user.getId();
    }

    public void withdrawUser(Long userId) {
        User user = getUser(userId);
        
        user.withdrawal();
    }

    public void modifyUser(Long userId, ModifyUserInfoFields modifyUserInfoFields) {
        User user = getUser(userId);

        user.updateNickname(modifyUserInfoFields.getNickname());
    }


    /* ### private method ### */
    private User getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별자를 가진 User가 없습니다. 요청한 User 식별값 : " + userId)
                );

        if (!user.isActivateUser()) {
            throw new CustomException("탈퇴한 회원입니다. 요청한 User 식별값 : " + userId, ErrorCode.ALREADY_WITHDRAW);
        }
        return user;
    }

}
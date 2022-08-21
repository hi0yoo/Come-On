package com.comeon.userservice.domain.user.service;

import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
import com.comeon.userservice.domain.user.dto.AccountDto;
import com.comeon.userservice.domain.user.dto.UserDto;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.repository.UserRepository;
import com.comeon.userservice.domain.user.utils.UserConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDto saveUser(UserDto userDto) {
        AccountDto accountDto = userDto.getAccountDto();
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
            User signupUser = UserConverter.toEntity(userDto);
            user = userRepository.save(signupUser);
        }

        return UserConverter.toDto(user);
    }

    public UserDto findUser(Long userId) {
        // TODO 탈퇴한 회원을 조회하면 오류 처리 어떻게?
        //  회원 조회시, 탈퇴한 회원과 애초에 존재하지 않는 회원의 응답을 다르게 할 필요성이 있는가?
        //  현재는 탈퇴한 회원도 애초에 존재하지 않는 회원으로 취급하여 EntityNotFoundException 발생시키도록 함.
        return userRepository.findById(userId)
                .map(UserConverter::toDto)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public void withdrawUser(Long userId) {
        // 소셜 로그인 계정 정보 삭제.
        // 이후, 사용자가 추가로 설정한 닉네임, 프로필 이미지 등은 지우는게 좋을지 그냥 두는게 좋을지..
        userRepository.findById(userId)
                .ifPresentOrElse(
                        User::withdrawal,
                        () -> {throw new EntityNotFoundException();}
                );
    }
}
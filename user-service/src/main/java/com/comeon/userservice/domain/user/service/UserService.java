package com.comeon.userservice.domain.user.service;

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
        Optional<User> findUser = userRepository.findByOAuthIdAndProvider(
                userDto.getOauthId(),
                userDto.getProvider()
        );

        User user = null;
        if (findUser.isPresent()) {
            user = findUser.orElseThrow();
            user.updateOAuthInfo(userDto.getEmail(), userDto.getName(), userDto.getProfileImgUrl());
        } else {
            User signupUser = UserConverter.toUserRoleEntity(userDto);
            user = userRepository.save(signupUser);
        }

        return UserConverter.toDto(user);
    }
}
package com.comeon.authservice.domain.user.service;

import com.comeon.authservice.domain.user.dto.UserDto;
import com.comeon.authservice.domain.user.entity.User;
import com.comeon.authservice.domain.user.repository.UserRepository;
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
    public User saveUser(UserDto userDto) {
        Optional<User> findUser = userRepository.findByOauthIdAndProviderName(
                userDto.getOauthId(),
                userDto.getProvider()
        );

        User user = null;
        if (findUser.isPresent()) {
            user = findUser.orElseThrow();
            user.updateOAuthInfo(userDto);
        } else {
            user = userRepository.save(new User(userDto));
        }

        return user;
    }
}

package com.comeon.authservice.domain.refreshtoken.service;

import com.comeon.authservice.domain.refreshtoken.dto.RefreshTokenDto;
import com.comeon.authservice.domain.refreshtoken.entity.RefreshToken;
import com.comeon.authservice.domain.refreshtoken.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken saveRefreshToken(RefreshTokenDto refreshTokenDto) {
        Optional<RefreshToken> findToken =
                refreshTokenRepository.findByUserId(refreshTokenDto.getUser().getId());

        RefreshToken refreshToken = null;
        if (findToken.isPresent()) {
            refreshToken = findToken.orElseThrow();
            refreshToken.updateToken(refreshTokenDto.getRefreshToken());
        } else {
            refreshToken = refreshTokenRepository.save(new RefreshToken(refreshTokenDto));
        }
        return refreshToken;
    }

    public Optional<RefreshToken> findRefreshToken(String token) {
        return refreshTokenRepository.findByTokenFetch(token);
    }

    @Transactional
    public void modifyRefreshToken(RefreshToken refreshToken, String generatedToken) {
        refreshToken.updateToken(generatedToken);
    }
}

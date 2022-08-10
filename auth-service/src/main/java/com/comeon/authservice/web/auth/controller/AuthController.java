package com.comeon.authservice.web.auth.controller;

import com.comeon.authservice.auth.jwt.exception.AccessTokenNotExpiredException;
import com.comeon.authservice.auth.jwt.JwtTokenProvider;
import com.comeon.authservice.auth.jwt.exception.InvalidJwtException;
import com.comeon.authservice.auth.jwt.exception.RefreshTokenNotExistException;
import com.comeon.authservice.domain.refreshtoken.entity.RefreshToken;
import com.comeon.authservice.domain.refreshtoken.service.RefreshTokenService;
import com.comeon.authservice.domain.user.entity.User;
import com.comeon.authservice.utils.CookieUtil;
import com.comeon.authservice.web.auth.dto.TokenReissueResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;

import static com.comeon.authservice.utils.CookieUtil.COOKIE_NAME_REFRESH_TOKEN;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    @Value("${jwt.refresh-token.expire-time}")
    private long refreshTokenExpirySec;

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/reissue")
    public ResponseEntity<TokenReissueResponseDto> reissueTokens(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = resolveAccessToken(request);

        // AccessToken 만료 확인
        if (!isAccessTokenExpired(accessToken)) {
            throw new AccessTokenNotExpiredException("만료되지 않은 Access Token은 재발급 할 수 없습니다.");
        }

        String refreshTokenValue = CookieUtil.getCookie(request, "refreshToken")
                .map(Cookie::getValue)
                .orElseThrow(() -> new RefreshTokenNotExistException("Refresh Token이 존재하지 않습니다."));

        // 유효한 Refresh Token 인지 검증. 유효하지 않으면 예외 발생
        jwtTokenProvider.validate(refreshTokenValue);

        // RefreshToken 유효하면 -> DB - RefreshToken 조회
        RefreshToken refreshToken = refreshTokenService.findRefreshToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidJwtException("Refresh Token이 유효하지 않습니다."));

        // RefreshToken 만료일 꺼내기
        Instant refreshTokenExpiration = jwtTokenProvider.getClaims(refreshToken.getToken()).getExpiration().toInstant();

        // RefreshToken 만료일 7일 미만이면 RefreshToken 재발급 및 저장, Cookie에 담음
        boolean refreshTokenReissuedFlag = false;
        if (Duration.between(Instant.now(), refreshTokenExpiration).toSeconds() < 60 * 60 * 24 * 7) {
            String generatedRefreshTokenValue = jwtTokenProvider.createRefreshToken(); // RefreshToken 생성
            refreshTokenService.modifyRefreshToken(refreshToken, generatedRefreshTokenValue); // 생성된 token 으로 값 변경
            // 쿠키에 담는다.
            CookieUtil.deleteCookie(request, response, COOKIE_NAME_REFRESH_TOKEN);
            CookieUtil.addCookie(response, COOKIE_NAME_REFRESH_TOKEN, refreshToken.getToken(), Long.valueOf(refreshTokenExpirySec).intValue());
            refreshTokenReissuedFlag = true;
        }

        // AccessToken 재발급
        User user = refreshToken.getUser();
        String generatedAccessToken = jwtTokenProvider.createAccessToken(
                user.getId().toString(),
                user.getRole().getRoleValue()
        );

        TokenReissueResponseDto responseDto = new TokenReissueResponseDto(generatedAccessToken, refreshTokenReissuedFlag);

        return ResponseEntity.ok(responseDto);
    }

    private boolean isAccessTokenExpired(String accessToken) {
        try {
            // AccessToken 검증에 성공하면 만료되지 않았으므로 false 반환
            jwtTokenProvider.validate(accessToken);
            return false;
        } catch (ExpiredJwtException e) { // AccessToken 만료 예외는 잡는다.
            return true;
        }
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

}

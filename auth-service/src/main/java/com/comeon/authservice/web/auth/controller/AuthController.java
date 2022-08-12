package com.comeon.authservice.web.auth.controller;

import com.comeon.authservice.auth.jwt.exception.AccessTokenNotExistException;
import com.comeon.authservice.auth.jwt.exception.AccessTokenNotExpiredException;
import com.comeon.authservice.auth.jwt.JwtTokenProvider;
import com.comeon.authservice.auth.jwt.exception.InvalidJwtException;
import com.comeon.authservice.auth.jwt.exception.RefreshTokenNotExistException;
import com.comeon.authservice.domain.refreshtoken.entity.RefreshToken;
import com.comeon.authservice.domain.refreshtoken.service.RefreshTokenService;
import com.comeon.authservice.utils.CookieUtil;
import com.comeon.authservice.web.auth.dto.TokenReissueResponse;
import com.comeon.authservice.web.common.response.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.NoSuchElementException;
import java.util.Optional;

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
    public ApiResponse<TokenReissueResponse> reissueTokens(HttpServletRequest request,
                                     HttpServletResponse response) {
        String accessToken = resolveAccessToken(request);
        RefreshToken refreshToken = resolveRefreshToken(request);

        Optional<String> reissuedRefreshToken = jwtTokenProvider.reissueRefreshToken(refreshToken.getToken());
        reissuedRefreshToken.ifPresent(jwt -> {
            refreshTokenService.modifyRefreshToken(refreshToken, jwt);
            CookieUtil.addCookie(response, COOKIE_NAME_REFRESH_TOKEN, jwt, Long.valueOf(refreshTokenExpirySec).intValue());
        });

        TokenReissueResponse reissueResponse = new TokenReissueResponse(jwtTokenProvider.reissueAccessToken(accessToken));

        return ApiResponse.createSuccess(reissueResponse);
    }

    private RefreshToken resolveRefreshToken(HttpServletRequest request) {
        String refreshTokenValue = CookieUtil.getCookie(request, "refreshToken")
                .map(Cookie::getValue)
                .orElseThrow(() -> new RefreshTokenNotExistException("Refresh Token이 존재하지 않습니다."));
        try {
            jwtTokenProvider.validate(refreshTokenValue);
            return refreshTokenService.findRefreshToken(refreshTokenValue);
        } catch (JwtException | NoSuchElementException e) {
            throw new InvalidJwtException("유효하지 않은 Refresh Token 입니다.", e);
        }
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (!(StringUtils.hasText(token) && token.startsWith("Bearer "))) {
            throw new AccessTokenNotExistException("Access Token이 존재하지 않습니다.");
        }

        String accessToken = token.substring(7);
        if (!isAccessTokenExpired(accessToken)) {
            throw new AccessTokenNotExpiredException("만료되지 않은 Access Token은 재발급 할 수 없습니다.");
        }

        return accessToken;
    }

    private boolean isAccessTokenExpired(String accessToken) {
        // 토큰 검증에 성공하면 만료되지 않았으므로 false, ExpiredJwtException 발생하면 토큰이 만료되었으므로 true.
        try {
            return !jwtTokenProvider.validate(accessToken);
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}

package com.comeon.authservice.web.auth.controller;

import com.comeon.authservice.auth.jwt.JwtTokenProvider;
import com.comeon.authservice.domain.refreshtoken.entity.RefreshToken;
import com.comeon.authservice.domain.refreshtoken.service.RefreshTokenService;
import com.comeon.authservice.utils.CookieUtil;
import com.comeon.authservice.web.auth.dto.TokenReissueResponse;
import com.comeon.authservice.web.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
                .orElseThrow();
        return refreshTokenService.findRefreshToken(refreshTokenValue);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        return request.getHeader("Authorization").substring(7);
    }
}

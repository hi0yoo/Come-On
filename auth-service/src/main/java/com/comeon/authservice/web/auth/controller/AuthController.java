package com.comeon.authservice.web.auth.controller;

import com.comeon.authservice.auth.jwt.JwtTokenProvider;
import com.comeon.authservice.auth.jwt.JwtRepository;
import com.comeon.authservice.utils.CookieUtil;
import com.comeon.authservice.web.auth.dto.TokenReissueResponse;
import com.comeon.authservice.web.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;

import static com.comeon.authservice.utils.CookieUtil.COOKIE_NAME_REFRESH_TOKEN;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    @Value("${jwt.refresh-token.expire-time}")
    private long refreshTokenExpirySec;

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRepository jwtRepository;

    @PostMapping("/reissue")
    public ApiResponse<TokenReissueResponse> reissueTokens(HttpServletRequest request,
                                                           HttpServletResponse response) {
        String accessToken = resolveAccessToken(request);
        String refreshToken = resolveRefreshToken(request);

        jwtTokenProvider.reissueRefreshToken(refreshToken)
                .ifPresent(jwt -> {
                    jwtRepository.addRefreshToken(
                            jwtTokenProvider.getUserId(accessToken),
                            jwt,
                            Duration.ofSeconds(refreshTokenExpirySec)
                    );
                    CookieUtil.addCookie(
                            response,
                            COOKIE_NAME_REFRESH_TOKEN,
                            jwt,
                            Long.valueOf(refreshTokenExpirySec).intValue()
                    );
                });

        TokenReissueResponse reissueResponse = new TokenReissueResponse(jwtTokenProvider.reissueAccessToken(accessToken));

        return ApiResponse.createSuccess(reissueResponse);
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        return CookieUtil.getCookie(request, "refreshToken")
                .map(Cookie::getValue)
                .orElseThrow();
    }

    private String resolveAccessToken(HttpServletRequest request) {
        return request.getHeader("Authorization").substring(7);
    }
}

package com.comeon.authservice.web.controller;

import com.comeon.authservice.common.jwt.JwtTokenInfo;
import com.comeon.authservice.common.jwt.JwtTokenProvider;
import com.comeon.authservice.common.jwt.JwtRepository;
import com.comeon.authservice.common.utils.CookieUtil;
import com.comeon.authservice.web.response.LogoutSuccessResponse;
import com.comeon.authservice.web.response.TokenReissueResponse;
import com.comeon.authservice.common.response.ApiResponse;
import com.comeon.authservice.web.response.ValidateMeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;

import static com.comeon.authservice.common.utils.CookieUtil.COOKIE_NAME_REFRESH_TOKEN;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRepository jwtRepository;

    @PostMapping("/reissue")
    public ApiResponse<TokenReissueResponse> reissueTokens(HttpServletRequest request,
                                                           HttpServletResponse response) {
        String accessToken = resolveAccessToken(request);
        String refreshToken = resolveRefreshToken(request);

        jwtTokenProvider.reissueRefreshToken(refreshToken)
                .ifPresent(jwt -> {
                    String jwtValue = jwt.getValue();
                    Duration jwtDuration = Duration.between(Instant.now(), jwt.getExpiry());

                    jwtRepository.addRefreshToken(
                            jwtTokenProvider.getUserId(accessToken),
                            jwtValue,
                            jwtDuration
                    );
                    CookieUtil.addCookie(
                            response,
                            COOKIE_NAME_REFRESH_TOKEN,
                            jwtValue,
                            Long.valueOf(jwtDuration.getSeconds()).intValue()
                    );
                });

        JwtTokenInfo accessTokenInfo = jwtTokenProvider.reissueAccessToken(accessToken);

        TokenReissueResponse reissueResponse = new TokenReissueResponse(
                accessTokenInfo.getValue(),
                accessTokenInfo.getExpiry().getEpochSecond()
        );

        return ApiResponse.createSuccess(reissueResponse);
    }

    @PostMapping("/logout")
    public ApiResponse<LogoutSuccessResponse> logout(HttpServletRequest request,
                                                     HttpServletResponse response) {
        String accessToken = resolveAccessToken(request);

        Instant expiration = jwtTokenProvider.getClaims(accessToken).getExpiration().toInstant();
        // 블랙 리스트에 추가. duration 만큼 지나면 자동 삭제.
        jwtRepository.addBlackList(accessToken, Duration.between(Instant.now(), expiration));
        // RefreshToken 삭제
        jwtRepository.removeRefreshToken(jwtTokenProvider.getUserId(accessToken));
        CookieUtil.deleteCookie(request, response, "refreshToken");

        return ApiResponse.createSuccess(new LogoutSuccessResponse("Logout Success"));
    }

    @PostMapping("/validate")
    public ApiResponse<ValidateMeResponse> validateMe(HttpServletRequest request) {
        String accessToken = resolveAccessToken(request);
        Long userId = Long.parseLong(jwtTokenProvider.getClaims(accessToken).getSubject());

        return ApiResponse.createSuccess(new ValidateMeResponse(userId));
    }


    /* === private method === */
    private String resolveRefreshToken(HttpServletRequest request) {
        return CookieUtil.getCookie(request, "refreshToken")
                .map(Cookie::getValue)
                .orElseThrow();
    }

    private String resolveAccessToken(HttpServletRequest request) {
        return request.getHeader("Authorization").substring(7);
    }
}

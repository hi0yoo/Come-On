package com.comeon.authservice.web.controller;

import com.comeon.authservice.common.jwt.JwtTokenInfo;
import com.comeon.authservice.common.jwt.JwtTokenProvider;
import com.comeon.authservice.common.jwt.RedisRepository;
import com.comeon.authservice.common.utils.CookieUtil;
import com.comeon.authservice.web.response.TokenReissueResponse;
import com.comeon.authservice.common.response.ApiResponse;
import com.comeon.authservice.web.response.ValidateMeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final RedisRepository redisRepository;

    @PostMapping("/reissue")
    public ApiResponse<TokenReissueResponse> reissueTokens(HttpServletRequest request,
                                                           HttpServletResponse response) {
        String accessToken = resolveAccessToken(request);
        String refreshToken = resolveRefreshToken(request);

        jwtTokenProvider.reissueRefreshToken(refreshToken)
                .ifPresent(jwt -> {
                    String jwtValue = jwt.getValue();
                    Duration jwtDuration = Duration.between(Instant.now(), jwt.getExpiry());

                    redisRepository.addRefreshToken(
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
        Long userId = Long.parseLong(jwtTokenProvider.getClaims(accessTokenInfo.getValue()).getSubject());

        TokenReissueResponse reissueResponse = new TokenReissueResponse(
                accessTokenInfo.getValue(),
                accessTokenInfo.getExpiry().getEpochSecond(),
                userId
        );

        return ApiResponse.createSuccess(reissueResponse);
    }

    @GetMapping("/validate")
    public ApiResponse<ValidateMeResponse> validateMe(HttpServletRequest request) {
        String accessToken = resolveAccessToken(request);
        Long userId = Long.parseLong(jwtTokenProvider.getClaims(accessToken).getSubject());

        log.info("[ValidateMe] Request User : {}", userId);

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

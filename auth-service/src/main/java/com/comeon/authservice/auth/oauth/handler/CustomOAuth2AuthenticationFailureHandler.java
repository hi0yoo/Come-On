package com.comeon.authservice.auth.oauth.handler;

import com.comeon.authservice.auth.oauth.repository.CustomAuthorizationRequestRepository;
import com.comeon.authservice.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.comeon.authservice.utils.CookieUtil.COOKIE_NAME_REDIRECT_URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final CustomAuthorizationRequestRepository authorizationRequestRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String redirectUri = CookieUtil.getCookie(request, COOKIE_NAME_REDIRECT_URI)
                .map(Cookie::getValue)
                .orElse("/");

        log.error("OAuth2 login fail", exception);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", exception.getLocalizedMessage())
                .build().toUriString();

        // auth 과정에서 생성한 쿠키들 삭제
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

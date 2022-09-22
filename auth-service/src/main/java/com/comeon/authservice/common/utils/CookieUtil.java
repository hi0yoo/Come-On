package com.comeon.authservice.common.utils;

import org.springframework.http.ResponseCookie;
import org.springframework.util.SerializationUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

// Cookie 작업을 편리하게 도와주는 Util
public class CookieUtil {

    public static final String COOKIE_NAME_OAUTH2_AUTHORIZATION_REQUEST = "oAuth2AuthorizationRequest";
    public static final String COOKIE_NAME_REDIRECT_URI = "redirectUri";
    public static final String COOKIE_NAME_REFRESH_TOKEN = "refreshToken";

    // TODO properties
    public static final String SERVER_DOMAIN = "api.come-on.ml";
    public static final String HEADER_SET_COOKIE = "Set-Cookie";

    public static Optional<Cookie> getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] requestCookies = request.getCookies();
        return requestCookies == null
                ? Optional.empty()
                : Arrays.stream(requestCookies)
                    .filter(cookie -> cookie.getName().equals(cookieName))
                    .findFirst();
    }

    public static void addCookie(HttpServletResponse response, String cookieName, String cookieValue, int maxAge) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setDomain(SERVER_DOMAIN);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    public static void addSecureCookie(HttpServletResponse response, String cookieName, String cookieValue, int maxAge) {
        ResponseCookie responseCookie = ResponseCookie.from(cookieName, cookieValue)
                .path("/")
                .domain(SERVER_DOMAIN)
                .maxAge(maxAge)
                .httpOnly(true)
                .secure(true)
                .sameSite(org.springframework.boot.web.server.Cookie.SameSite.NONE.attributeValue())
                .build();

        response.addHeader(HEADER_SET_COOKIE, responseCookie.toString());
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    public static String serialize(Object obj) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(obj));
    }

    public static <T> T deserialize(Cookie cookie, Class<T> clazz) {
        return clazz.cast(
                SerializationUtils.deserialize(
                        Base64.getUrlDecoder().decode(cookie.getValue())
                )
        );
    }
}

package com.comeon.authservice.auth.filter;

import com.comeon.authservice.auth.jwt.exception.AccessTokenNotExpiredException;
import com.comeon.authservice.web.common.response.ApiResponse;
import com.comeon.authservice.web.common.response.ErrorCode;
import com.comeon.authservice.web.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // TODO error code 지정
        try {
            filterChain.doFilter(request, response);
        } catch (AccessTokenNotExpiredException e) {
            log.error("AccessToken 만료 안됨");
            setResponse(response, SC_BAD_REQUEST, ApiResponse.createBadParameter(ErrorCode.createErrorCode(e)));
        } catch (RuntimeException e) {
            setResponse(response, SC_UNAUTHORIZED, ApiResponse.createUnauthorized(ErrorCode.createErrorCode(e)));
        }
    }

    private void setResponse(HttpServletResponse response,
                             int httpStatusCode,
                             ApiResponse<ErrorResponse> responseBody) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(httpStatusCode);
        response.getWriter().write(objectMapper.writer().writeValueAsString(responseBody));
    }
}

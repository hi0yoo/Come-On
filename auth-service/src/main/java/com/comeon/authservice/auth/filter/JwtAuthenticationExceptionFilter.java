package com.comeon.authservice.auth.filter;

import com.comeon.authservice.auth.exception.AuthorizationHeaderException;
import com.comeon.authservice.web.common.response.ApiResponse;
import com.comeon.authservice.web.common.response.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.comeon.authservice.web.common.response.ErrorCode.*;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Slf4j
public class JwtAuthenticationExceptionFilter extends AbstractAuthenticationExceptionFilter {

    public JwtAuthenticationExceptionFilter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        ErrorCode errorCode = INTERNAL_SERVER_ERROR;
        // TODO 로깅
        try {
            filterChain.doFilter(request, response);
        } catch (AuthorizationHeaderException e) {
            errorCode = NOT_EXIST_AUTHORIZATION_HEADER;
        } catch (JwtException e) {
            errorCode = INVALID_ACCESS_TOKEN;
        }

        setResponse(response, SC_UNAUTHORIZED, ApiResponse.createUnauthorized(errorCode));
    }
}

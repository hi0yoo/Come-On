package com.comeon.authservice.config.security.filter;

import com.comeon.authservice.common.exception.CustomException;
import com.comeon.authservice.common.response.ApiResponse;
import com.comeon.authservice.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.comeon.authservice.common.exception.ErrorCode.*;

@Slf4j
public class JwtAuthenticationExceptionFilter extends AbstractAuthenticationExceptionFilter {

    public JwtAuthenticationExceptionFilter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (CustomException e) {
            log.error("[{}] {}", e.getClass().getSimpleName(), e.getMessage(), e);
            ErrorCode errorCode = e.getErrorCode();
            setResponse(response, errorCode.getHttpStatus().value(), ApiResponse.createError(errorCode));
        } catch (JwtException e) {
            log.error("[{}] {}", e.getClass().getSimpleName(), e.getMessage(), e);
            ErrorCode errorCode = INVALID_ACCESS_TOKEN;
            setResponse(response, errorCode.getHttpStatus().value(), ApiResponse.createError(errorCode));
        } catch (Exception e) {
            log.error("[{}] {}", e.getClass().getSimpleName(), e.getMessage(), e);
            ErrorCode errorCode = INTERNAL_SERVER_ERROR;
            setResponse(response, errorCode.getHttpStatus().value(), ApiResponse.createError(errorCode));
        }
    }
}

package com.comeon.authservice.auth.filter;

import com.comeon.authservice.auth.exception.*;
import com.comeon.authservice.web.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.comeon.authservice.web.common.response.ErrorCode.*;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Slf4j
public class ReissueAuthenticationExceptionFilter extends AbstractAuthenticationExceptionFilter {

    public ReissueAuthenticationExceptionFilter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // TODO 로깅
        try {
            filterChain.doFilter(request, response);
        } catch (AuthorizationHeaderException e) {
            setResponse(response, SC_UNAUTHORIZED, ApiResponse.createUnauthorized(NOT_EXIST_AUTHORIZATION_HEADER));
        } catch (InvalidAccessTokenException e) {
            setResponse(response, SC_UNAUTHORIZED, ApiResponse.createUnauthorized(INVALID_ACCESS_TOKEN));
        } catch (RefreshTokenNotExistException e) {
            setResponse(response, SC_UNAUTHORIZED, ApiResponse.createUnauthorized(NOT_EXIST_REFRESH_TOKEN));
        } catch (InvalidRefreshTokenException e) {
            setResponse(response, SC_UNAUTHORIZED, ApiResponse.createUnauthorized(INVALID_REFRESH_TOKEN));
        } catch (AccessTokenNotExpiredException e) {
            setResponse(response, SC_BAD_REQUEST, ApiResponse.createBadRequest(NOT_EXPIRED_ACCESS_TOKEN));
        }
    }
}

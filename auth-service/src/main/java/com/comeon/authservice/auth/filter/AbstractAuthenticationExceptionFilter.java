package com.comeon.authservice.auth.filter;

import com.comeon.authservice.web.common.response.ApiResponse;
import com.comeon.authservice.web.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractAuthenticationExceptionFilter extends OncePerRequestFilter {

    protected final ObjectMapper objectMapper;

    protected AbstractAuthenticationExceptionFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected void setResponse(HttpServletResponse response,
                             int httpStatusCode,
                             ApiResponse<ErrorResponse> responseBody) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(httpStatusCode);
        response.getWriter().write(objectMapper.writer().writeValueAsString(responseBody));
    }
}

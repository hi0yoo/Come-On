package com.comeon.courseservice.config.argresolver;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtArgumentResolver implements HandlerMethodArgumentResolver {

    @Value("${token.claim-name.user-id}")
    private String userIdClaimName;

    private final ObjectMapper objectMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasUserIdAnnotation = parameter.hasParameterAnnotation(CurrentUserId.class);
        boolean isLongType = Long.class.isAssignableFrom(parameter.getParameterType());
        return hasUserIdAnnotation && isLongType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        String accessToken = resolveAccessToken(request);

        // 토큰이 없다면 null 반환
        return StringUtils.hasText(accessToken) ? getUserId(accessToken) : null;
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(token)) {
            return null;
        }

        if (!token.startsWith("Bearer ")) {
            throw new CustomException("올바르지 않은 인증 헤더입니다.", ErrorCode.INVALID_AUTHORIZATION_HEADER);
        }

        return token.substring(7);
    }

    private Long getUserId(String accessToken) {
        Base64.Decoder urlDecoder = Base64.getUrlDecoder();
        String payload = new String(urlDecoder.decode(accessToken.split("\\.")[1]));
        Map<String, Object> payloadObject = null;
        try {
            payloadObject = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new CustomException("토큰 파싱 오류 발생", e, ErrorCode.SERVER_ERROR);
        }

        return Long.parseLong((String) payloadObject.get(userIdClaimName));
    }
}

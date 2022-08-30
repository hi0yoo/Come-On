package com.comeon.meetingservice.web.common.interceptor.pathcheck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class PathMatcherInterceptor implements HandlerInterceptor {

    private final HandlerInterceptor handlerInterceptor;
    private final PathContainer pathContainer;

    public PathMatcherInterceptor(HandlerInterceptor handlerInterceptor) {
        this.handlerInterceptor = handlerInterceptor;
        this.pathContainer = new PathContainer();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        log.info("[PathMatcherInterceptor] 발동");
        if (pathContainer.notIncludedPath(request.getRequestURI(), request.getMethod())) {
            return true;
        }
        return handlerInterceptor.preHandle(request, response, handler);
    }

    public PathMatcherInterceptor includePathPattern(String pathPattern, PathMethod pathMethod) {
        pathContainer.includePathPattern(pathPattern, pathMethod);
        return this;
    }

    public PathMatcherInterceptor excludePathPattern(String pathPattern, PathMethod pathMethod) {
        pathContainer.excludePathPattern(pathPattern, pathMethod);
        return this;
    }
}
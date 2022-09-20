package com.comeon.meetingservice.web.config;

import com.comeon.meetingservice.web.common.argumentresolver.UserIdArgumentResolver;
import com.comeon.meetingservice.web.common.interceptor.MeetingAuthInterceptor;
import com.comeon.meetingservice.web.meetinguser.query.MeetingUserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final MeetingUserQueryRepository meetingUserQueryRepository;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserIdArgumentResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MeetingAuthInterceptor(meetingUserQueryRepository))
                .excludePathPatterns("/docs/**", "/favicon.ico", "/error");
    }

}

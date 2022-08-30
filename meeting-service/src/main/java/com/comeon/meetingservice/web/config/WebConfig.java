package com.comeon.meetingservice.web.config;

import com.comeon.meetingservice.web.common.argumentresolver.UserIdArgumentResolver;
import com.comeon.meetingservice.web.common.interceptor.custom.HostUserCheckInterceptor;
import com.comeon.meetingservice.web.common.interceptor.custom.MeetingUserCheckInterceptor;
import com.comeon.meetingservice.web.common.interceptor.pathcheck.PathMatcherInterceptor;
import com.comeon.meetingservice.web.common.interceptor.pathcheck.PathMethod;
import com.comeon.meetingservice.web.meetinguser.query.MeetingUserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
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
        registry.addInterceptor(meetingUserCheckInterceptor(meetingUserQueryRepository));
        registry.addInterceptor(hostUserCheckInterceptor(meetingUserQueryRepository));
    }

    private HandlerInterceptor meetingUserCheckInterceptor(MeetingUserQueryRepository meetingUserQueryRepository) {
        return new PathMatcherInterceptor(new MeetingUserCheckInterceptor(meetingUserQueryRepository))
                .includePathPattern("/meetings/{meetingId}", PathMethod.DELETE)
                .includePathPattern("/meetings/{meetingId}", PathMethod.GET)
                .includePathPattern("/meetings/{meetingId}/dates", PathMethod.POST)
                .includePathPattern("/meetings/{meetingId}/dates/{dateId}", PathMethod.DELETE)
                .includePathPattern("/meetings/{meetingId}/dates/{dateId}", PathMethod.GET)
                .includePathPattern("/meetings/{meetingId}/places/", PathMethod.POST)
                .includePathPattern("/meetings/{meetingId}/places/{placeId}", PathMethod.PATCH)
                .includePathPattern("/meetings/{meetingId}/places/{placeId}", PathMethod.DELETE);

    }

    private HandlerInterceptor hostUserCheckInterceptor(MeetingUserQueryRepository meetingUserQueryRepository) {
        return new PathMatcherInterceptor(new HostUserCheckInterceptor(meetingUserQueryRepository))
                .includePathPattern("/meetings/{meetingId}", PathMethod.POST)
                .includePathPattern("/meetings/{meetingId}/dates/{dateId}", PathMethod.PATCH)
                .excludePathPattern("/meetings/users", PathMethod.POST);
    }
}

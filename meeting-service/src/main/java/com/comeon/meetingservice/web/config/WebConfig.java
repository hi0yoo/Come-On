package com.comeon.meetingservice.web.config;

import com.comeon.meetingservice.web.common.argumentresolver.UserIdArgumentResolver;
import com.comeon.meetingservice.web.common.interceptor.custom.HostUserCheckInterceptor;
import com.comeon.meetingservice.web.common.interceptor.custom.MeetingUserCheckInterceptor;
import com.comeon.meetingservice.web.common.interceptor.pathcheck.PathMatcherInterceptor;
import com.comeon.meetingservice.web.meetinguser.query.MeetingUserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static com.comeon.meetingservice.web.common.interceptor.pathcheck.PathMethod.*;

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
                .includePathPattern("/meetings/{meetingId}", DELETE)
                .includePathPattern("/meetings/{meetingId}", GET)
                .includePathPattern("/meetings/{meetingId}/dates", POST)
                .includePathPattern("/meetings/{meetingId}/dates/{dateId}", DELETE)
                .includePathPattern("/meetings/{meetingId}/dates/{dateId}", GET)
                .includePathPattern("/meetings/{meetingId}/places/", POST)
                .includePathPattern("/meetings/{meetingId}/places/{placeId}", PATCH)
                .includePathPattern("/meetings/{meetingId}/places/{placeId}", DELETE);

    }

    private HandlerInterceptor hostUserCheckInterceptor(MeetingUserQueryRepository meetingUserQueryRepository) {
        return new PathMatcherInterceptor(new HostUserCheckInterceptor(meetingUserQueryRepository))
                .includePathPattern("/meetings/{meetingId}/codes/{codeId}", PATCH)
                .includePathPattern("/meetings/{meetingId}", POST)
                .includePathPattern("/meetings/{meetingId}/dates/{dateId}", PATCH)
                .includePathPattern("/meetings/{meetingId}/users/{userId}", PATCH)
                .excludePathPattern("/meetings/users", POST);
    }
}

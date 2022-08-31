package com.comeon.courseservice.config;

import com.comeon.courseservice.config.custommock.MockUserServiceFeignClient;
import com.comeon.courseservice.web.common.file.FileManager;
import com.comeon.courseservice.web.course.query.CourseQueryRepository;
import com.comeon.courseservice.web.course.query.CourseQueryService;
import com.comeon.courseservice.web.user.service.UserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MockFeignClientConfig {

    @Bean
    public UserService userService() {
        return new MockUserServiceFeignClient();
    }

    @Bean(name = "mockCourseQueryService")
    public CourseQueryService courseQueryServiceMock(FileManager fileManager, CourseQueryRepository courseQueryRepository) {
        return new CourseQueryService(fileManager, userService(), courseQueryRepository);
    }
}

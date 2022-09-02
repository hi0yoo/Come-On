package com.comeon.userservice.test;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @PostMapping("/user-test-api/users/init")
    public List<Long> initUsers() {
        return testService.initUser();
    }
}

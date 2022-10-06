package com.comeon.userservice.web;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JenkinsTest {

    @Test
    void fail() {
        // 젠킨스 ci/cd 테스트를 위한 실패 케이스
        assertThat(true).isFalse(); // 실패
    }
}

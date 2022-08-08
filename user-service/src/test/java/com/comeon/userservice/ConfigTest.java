package com.comeon.userservice;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest
public class ConfigTest {

    @Autowired
    Environment env;

    @Test
    public void 연동_테스트() throws Exception {
        // given

        // when
        String property = env.getProperty("test.value");

        // then
        Assertions.assertThat(property).isEqualTo("test");
    }
}

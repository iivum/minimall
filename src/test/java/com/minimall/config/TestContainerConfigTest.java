package com.minimall.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TestContainerConfigTest {

    @Test
    void postgresContainerStartsSuccessfully(@Autowired ApplicationContext context) {
        assertThat(context).isNotNull();
    }
}
package com.minimall.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;

class AsyncConfigTest {

    @Test
    @DisplayName("taskExecutor bean should be configured with bounded queue")
    void taskExecutor_boundedQueueCapacity() {
        AsyncConfig config = new AsyncConfig();
        Executor executor = config.taskExecutor();

        assertInstanceOf(ThreadPoolTaskExecutor.class, executor);
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;

        assertTrue(taskExecutor.getQueueCapacity() > 0, "queueCapacity must be > 0");
    }

    @Test
    @DisplayName("taskExecutor should use CallerRunsPolicy")
    void taskExecutor_callerRunsPolicy() {
        AsyncConfig config = new AsyncConfig();
        Executor executor = config.taskExecutor();

        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        assertNotNull(taskExecutor.getRejectedExecutionHandler());

        assertInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class,
            taskExecutor.getRejectedExecutionHandler(),
            "RejectedExecutionHandler must be CallerRunsPolicy");
    }

    @Test
    @DisplayName("taskExecutor should have correct pool size settings")
    void taskExecutor_poolSizeSettings() {
        AsyncConfig config = new AsyncConfig();
        Executor executor = config.taskExecutor();

        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        assertEquals(4, taskExecutor.getCorePoolSize());
        assertEquals(8, taskExecutor.getMaxPoolSize());
    }
}
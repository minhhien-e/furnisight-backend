package com.furniro.MessageService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    @Primary
    @Bean(name = "taskExecutor")
    TaskExecutor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5);
        
        executor.setMaxPoolSize(10);
        
        executor.setQueueCapacity(100);
        
        executor.setThreadNamePrefix("AsyncThread-");
        
        executor.initialize();
        return executor;
    }
}
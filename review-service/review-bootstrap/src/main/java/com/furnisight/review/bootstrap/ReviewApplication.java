package com.furnisight.review.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = "com.furnisight.review")
@EnableAsync
@EnableCaching
@EntityScan(basePackages = "com.furnisight.review.domain")
@EnableJpaRepositories(basePackages = "com.furnisight.review.adapter.out.repository")
public class ReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewApplication.class, args);
    }
}

package com.furnisight.review.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.furnisight.review")
@EnableAsync
@EntityScan(basePackages = "com.furnisight.review.domain")
@EnableJpaRepositories(basePackages = "com.furnisight.review.adapter.database.repository.jpa")
public class ReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewApplication.class, args);
    }
}

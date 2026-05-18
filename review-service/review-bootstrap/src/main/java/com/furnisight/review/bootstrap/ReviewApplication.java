package com.furnisight.review.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;
@SpringBootApplication(scanBasePackages = "com.furnisight.review")
public class ReviewApplication {

    private static final String DEFAULT_TIMEZONE = "UTC";

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
        SpringApplication.run(ReviewApplication.class, args);
    }
}

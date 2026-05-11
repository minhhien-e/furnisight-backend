package com.furnisight.user.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.furnisight.user.infrastructure.database.repository.jpa")
@EnableJpaAuditing
public class JpaConfig {
}

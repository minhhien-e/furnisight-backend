package com.furnisight.media.infrastructure.config;


import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@Configuration
@EnableJpaRepositories(basePackages = "com.furnisight.media.infrastructure.repository")
@EntityScan(basePackages = "com.furnisight.media.core.model.entity")
public class JpaConfig {
}

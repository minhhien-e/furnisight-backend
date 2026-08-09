package com.furnisight.catalog.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.furnisight.catalog.infrastructure.database.repository.jpa")
@EntityScan(basePackages = "com.furnisight.catalog.domain.entities")
public class JpaConfig {
}

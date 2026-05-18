package com.furnisight.review.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
@EnableJpaRepositories(
    basePackages ={ "com.furnisight.review.infrastructure.database.repository.jpa"}
)
@EntityScan(
    basePackages = {
        "com.furnisight.review.core.model.entity",
    }
)
public class PersistenceConfig {
}

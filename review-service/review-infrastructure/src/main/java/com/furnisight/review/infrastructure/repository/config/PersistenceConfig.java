package com.furnisight.review.infrastructure.repository.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
@EnableJpaRepositories(
    basePackages ={ "com.furnisight.review.infrastructure.repository.persistence.write.jpa",
    "com.furnisight.review.infrastructure.repository.persistence.read.jpa"}
)
@EntityScan(
    basePackages = {
        "com.furnisight.review.core.domain",
        "com.furnisight.review.infrastructure.entity"
    }
)
public class PersistenceConfig {
}

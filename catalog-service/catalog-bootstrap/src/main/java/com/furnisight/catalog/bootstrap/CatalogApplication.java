package com.furnisight.catalog.bootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.furnisight.catalog")
@EnableScheduling
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.furnisight.catalog.infrastructure.database.repository.jpa")
@EnableElasticsearchRepositories(basePackages = "com.furnisight.catalog.infrastructure.elasticsearch.repository")
@EntityScan(basePackages = "com.furnisight.catalog.domain.entities")
public class CatalogApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }
}

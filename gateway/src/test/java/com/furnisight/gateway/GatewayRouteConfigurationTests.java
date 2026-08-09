package com.furnisight.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class GatewayRouteConfigurationTests {

    @Test
    void catalogRouteUsesGateway2024ConfigurationPrefix() throws IOException {
        List<PropertySource<?>> propertySources = new YamlPropertySourceLoader()
            .load("gateway-routes", new ClassPathResource("application.yaml"));

        assertThat(propertySources)
            .extracting(source -> source.getProperty("spring.cloud.gateway.routes[1].id"))
            .contains("catalog-service");
        assertThat(propertySources)
            .extracting(source -> source.getProperty(
                "spring.cloud.gateway.routes[1].filters[0].args.replacement"))
            .contains("/api/v1/$\\{segment}");
        assertThat(propertySources)
            .extracting(source -> source.getProperty("spring.cloud.gateway.server.webflux.routes[1].id"))
            .containsOnlyNulls();
        assertThat(propertySources)
            .extracting(source -> source.getProperty(
                "spring.cloud.gateway.routes[7].filters[0].args.replacement"))
            .contains("/$\\{segment}");
    }
}

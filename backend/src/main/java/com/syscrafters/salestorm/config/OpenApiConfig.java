package com.syscrafters.salestorm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI salestormOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SALESTORM | SYSCRAFTERS 2026 High-Scale Flash-Sale Architecture")
                        .description("System Design Prototype demonstrating atomic inventory reservation, idempotency, transactional outbox pattern, Kafka event bus, and resilience.")
                        .version("1.0.0")
                        .contact(new Contact().name("SYSCRAFTERS 2026").email("contact@syscrafters.io"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}

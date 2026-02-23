package com.exam.library_management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI libraryManagementOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Management API")
                        .description("APIs for authentication, book management and borrowing flows")
                        .version("v1")
                        .contact(new Contact().name("Library Management Team")));
    }
}

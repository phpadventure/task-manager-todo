package com.taskmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        String description = "REST API for managing tasks across multiple lists with OAuth2 "
            + "authentication. Users can create task lists, organize tasks, search, filter, and "
            + "receive real-time notifications for overdue and upcoming tasks.";
        return new OpenAPI()
            .info(new Info()
                .title("Personal To-Do Manager API")
                .version("1.0.0")
                .description(description)
                .contact(new Contact()
                    .name("Development Team")
                    .url("https://capgemini.com")));
    }
}

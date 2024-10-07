package com.javaweb.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI springShopOpenAPI(){
        return new OpenAPI().info(new Info().title("E-Commerce")
                .description("Backend APIs for E-Commerce app")
                .version("1.0.0")
                .contact(new Contact().name("Chu Ngọc Thắng").email("chuthanglsz@gmail.com"))
                .license(new License().name("License").url("/")))
                .externalDocs(new ExternalDocumentation().description("E-Commerce")
                        .url("http://localhost:8085/swagger-ui.html"));
    }
}

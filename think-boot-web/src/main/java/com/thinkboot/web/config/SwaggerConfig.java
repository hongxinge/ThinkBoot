package com.thinkboot.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    @ConditionalOnProperty(prefix = "think-boot.swagger", name = "enabled", havingValue = "true")
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ThinkBoot API")
                        .description("ThinkBoot API Documentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ThinkBoot")
                                .url("https://github.com/thinkboot")));
    }
}
package com.studiolynk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI studioLynkOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("StudioLynk Backend API")
                        .description("REST API documentation for StudioLynk — Photography Studio & Freelancer Collaboration Platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("StudioLynk Engineering Team")
                                .email("engineering@studiolynk.local"))
                        .license(new License()
                                .name("Academic Prototype License")));
    }
}

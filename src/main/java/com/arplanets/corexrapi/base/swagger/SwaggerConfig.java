package com.arplanets.corexrapi.base.swagger;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SwaggerConfig {

    public static final String PRODUCT_ID = "CoreXR";

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .info(new Info()
                        .title(PRODUCT_ID + " API Documentation")
                        .version("1.0.0")
                        .description("API Document for " + PRODUCT_ID)
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http:/springdoc.org")
                        )
                );
    }

    @Bean
    public OpenApiCustomizer customizeOpenApi() {
        return openApi -> openApi.getComponents().getSchemas().forEach((name, schema) -> {
            if (schema.getProperties() != null) {
                PropertyNamingStrategies.SnakeCaseStrategy strategy = new PropertyNamingStrategies.SnakeCaseStrategy();
                Map<String, Schema> newProperties = new HashMap<>();
                schema.getProperties().forEach((propName, propSchema) -> {
                    // 將 Object 顯式轉換為 Schema
                    newProperties.put(strategy.translate(propName.toString()), (Schema) propSchema);
                });
                schema.setProperties(newProperties);
            }
        });
    }
}

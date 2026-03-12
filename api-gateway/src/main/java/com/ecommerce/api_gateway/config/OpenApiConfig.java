package com.ecommerce.api_gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI gatewayOpenApi(
      @Value("${info.app.name:api-gateway}") String name,
      @Value("${info.app.description:API Gateway Swagger aggregation}") String description,
      @Value("${info.app.version:1.0.0}") String version) {
    return new OpenAPI().info(new Info().title(name).description(description).version(version));
  }
}

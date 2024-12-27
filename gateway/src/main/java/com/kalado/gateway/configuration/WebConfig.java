package com.kalado.gateway.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOrigins(
            "http://localhost:3000",
            "https://kalado.app",
            "http://kaladoshop.com",
            "http://192.168.34.152:5173",
            "http://localhost:5173"
        )
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        .allowedHeaders("*")
        .allowCredentials(true) // Allows cookies/auth headers
        .maxAge(3600); // Cache preflight response for 1 hour
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
    return new FeignClientErrorDecoder(objectMapper);
  }
}

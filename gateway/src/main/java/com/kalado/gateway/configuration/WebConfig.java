package com.kalado.gateway.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        // .addMapping("/**")

        // // .allowedOrigins("*")
        // .allowedOrigins("http://localhost:8080", "http://kaladoshop.com")



        // .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")

        // .allowedHeaders("*")

        // .allowCredentials(true)

        // .maxAge(3600);
      .addMapping("/**")

      .allowedOrigins(
          "http://localhost:8080",  
          "http://kaladoshop.com/"
      )

      .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")

      .allowedHeaders("*")

      .maxAge(3600);
  }

//  @Override
//  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
//    configurer.favorParameter(true)
//            .parameterName("mediaType")
//            .defaultContentType(MediaType.APPLICATION_JSON)
//            .mediaType("json", MediaType.APPLICATION_JSON)
//            .mediaType("multipart", MediaType.MULTIPART_FORM_DATA);
//  }
//

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
    return new FeignClientErrorDecoder(objectMapper);
  }
}

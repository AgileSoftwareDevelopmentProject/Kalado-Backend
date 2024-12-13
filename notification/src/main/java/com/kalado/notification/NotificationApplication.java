package com.kalado.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.kalado.common.feign")
@SpringBootApplication(scanBasePackages = {"com.kalado.notification", "com.kalado.common"})
@EnableEurekaClient
public class NotificationApplication {
  public static void main(String[] args) {
    SpringApplication.run(NotificationApplication.class, args);
  }
} 

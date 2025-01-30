package com.kalado.reporting;

import com.kalado.common.feign.product.ProductApi;
import com.kalado.common.feign.user.UserApi;
import com.kalado.reporting.application.service.EmailService;
import com.kalado.reporting.application.service.ReportProductHandler;
import com.kalado.reporting.application.service.ReportStatusUpdater;
import com.kalado.reporting.application.service.ReportUserHandler;
import com.kalado.reporting.domain.model.ReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class ReportTestConfig {

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }

    @Bean
    @Primary
    public EmailService emailService(JavaMailSender javaMailSender, UserApi userApi) {
        return new EmailService(javaMailSender, userApi);
    }

    @Value("${app.upload.dir}")
    private String uploadDir;

    @PostConstruct
    public void init() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }
    @Bean
    @Primary
    public ReportStatusUpdater reportStatusUpdater(
            ReportRepository reportRepository,
            ReportUserHandler userApi,
            ReportProductHandler productApi
    ) {
        return new ReportStatusUpdater(reportRepository, userApi, productApi);
    }

}
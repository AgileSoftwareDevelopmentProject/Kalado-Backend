package com.kalado.authentication;

import com.kalado.authentication.application.service.EmailService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        // Return a non-functional mail sender for tests
        return new JavaMailSenderImpl();
    }

    @Bean
    @Primary
    public EmailService emailService(JavaMailSender javaMailSender) {
        // Create a test version of EmailService that doesn't actually send emails
        return new EmailService(javaMailSender) {
            @Override
            public void sendVerificationToken(String to, String token) {
                // Do nothing in tests
            }

            @Override
            public void sendPasswordResetToken(String to, String token) {
                // Do nothing in tests
            }
        };
    }
}
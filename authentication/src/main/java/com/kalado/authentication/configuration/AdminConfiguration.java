package com.kalado.authentication.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@Configuration
public class AdminConfiguration {
    private Set<String> authorizedAdminEmails = new HashSet<>();
    private Set<String> authorizedGodEmails = new HashSet<>();

    @PostConstruct
    public void loadAuthorizedEmails() {
        try {
            Properties properties = loadProperties("admin-emails.properties");

            authorizedAdminEmails = parseEmails(properties.getProperty("authorized.admin.emails", ""));
            authorizedGodEmails = parseEmails(properties.getProperty("authorized.god.emails", ""));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load authorized emails", e);
        }
    }

    private Properties loadProperties(String fileName) throws IOException {
        Resource resource = new ClassPathResource(fileName);
        Properties properties = new Properties();
        properties.load(resource.getInputStream());
        return properties;
    }

    private Set<String> parseEmails(String emailsString) {
        Set<String> emails = new HashSet<>();
        String[] emailsArray = emailsString.split(",");
        for (String email : emailsArray) {
            String trimmedEmail = email.trim();
            if (!trimmedEmail.isEmpty()) {
                emails.add(trimmedEmail.toLowerCase());
            }
        }
        return emails;
    }

    public boolean isEmailAuthorizedForAdmin(String email) {
        return email != null && authorizedAdminEmails.contains(email.toLowerCase());
    }

    public boolean isEmailAuthorizedForGod(String email) {
        return email != null && authorizedGodEmails.contains(email.toLowerCase());
    }

    public Set<String> getAuthorizedAdminEmails() {
        return new HashSet<>(authorizedAdminEmails);
    }

    public Set<String> getAuthorizedGodEmails() {
        return new HashSet<>(authorizedGodEmails);
    }
}
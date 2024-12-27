package com.kalado.reporting.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties(prefix = "app.upload")
public class UploadConfiguration {
    private String dir;
    private DataSize maxFileSize;
    private int maxFiles;

    @PostConstruct
    public void init() throws IOException {
        Path uploadPath = Paths.get(dir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public DataSize getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(DataSize maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public int getMaxFiles() {
        return maxFiles;
    }

    public void setMaxFiles(int maxFiles) {
        this.maxFiles = maxFiles;
    }
}
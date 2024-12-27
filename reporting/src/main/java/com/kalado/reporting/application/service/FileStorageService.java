package com.kalado.reporting.application.service;

import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public String storeFile(MultipartFile file) {
        validateFile(file);

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);

            return fileName;
        } catch (IOException ex) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Could not store file");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "File cannot be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "File size cannot exceed 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !isValidContentType(contentType)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "Invalid file type");
        }
    }

    private boolean isValidContentType(String contentType) {
        return contentType.startsWith("image/") ||
                contentType.equals("application/pdf") ||
                contentType.equals("text/plain");
    }
}
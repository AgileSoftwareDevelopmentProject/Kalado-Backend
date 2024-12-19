package com.kalado.product.application.service;

import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String storeImage(MultipartFile file) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);

            // Save file
            Files.copy(file.getInputStream(), filePath);
            log.info("Stored image: {}", filename);

            return filename;
        } catch (IOException e) {
            log.error("Failed to store image", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to store image");
        }
    }

    public void deleteImage(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
            log.info("Deleted image: {}", filename);
        } catch (IOException e) {
            log.error("Failed to delete image: {}", filename, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to delete image");
        }
    }
}
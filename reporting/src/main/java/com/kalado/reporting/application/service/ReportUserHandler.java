package com.kalado.reporting.application.service;

import com.kalado.common.dto.ProductDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.product.ProductApi;
import com.kalado.common.feign.user.UserApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportUserHandler {
    private final UserApi userApi;
    private final ProductApi productApi;


    public void blockUser(Long userId) {
        try {
            userApi.blockUser(userId);
        } catch (Exception e) {
            throw new CustomException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to block user: " + userId
            );
        }
    }

    public List<ProductDto> getUserProducts(Long userId) {
        try {
            return productApi.getSellerProducts(userId);
        } catch (Exception e) {
            throw new CustomException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve user's products"
            );
        }
    }
}
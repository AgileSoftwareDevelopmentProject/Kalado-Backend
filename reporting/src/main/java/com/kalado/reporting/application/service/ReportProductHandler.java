package com.kalado.reporting.application.service;

import com.kalado.common.dto.ProductDto;
import com.kalado.common.dto.ProductStatusUpdateDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.ProductStatus;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.product.ProductApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportProductHandler {
    private final ProductApi productApi;

    public void blockProduct(Long productId, Long sellerId) {
        try {
            productApi.updateProductStatus(
                    productId,
                    sellerId,
                    new ProductStatusUpdateDto(ProductStatus.DELETED)
            );
        } catch (Exception e) {
            throw new CustomException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to block product: " + productId
            );
        }
    }

    public void blockMultipleProducts(List<ProductDto> products) {
        products.forEach(product ->
                blockProduct(product.getId(), product.getSellerId())
        );
    }
}

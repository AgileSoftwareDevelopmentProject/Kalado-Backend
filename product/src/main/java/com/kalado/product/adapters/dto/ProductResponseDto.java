package com.kalado.product.adapters.dto;

import com.kalado.common.Price;
import com.kalado.common.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String title;
    private String description;
    private Price price;
//    private List<String> imageUrls;
    private String category;
    private Integer productionYear;
    private String brand;
    private ProductStatus status;
    private Timestamp createdAt;
    private Long sellerId;
}


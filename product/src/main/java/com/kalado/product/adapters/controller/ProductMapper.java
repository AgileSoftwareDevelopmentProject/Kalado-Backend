package com.kalado.product.adapters.controller;

import com.kalado.common.dto.ProductDto;
import com.kalado.product.adapters.dto.ProductCreateRequestDto;
import com.kalado.product.adapters.dto.ProductResponseDto;
import com.kalado.product.adapters.dto.ProductUpdateRequestDto;
import com.kalado.product.domain.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
  // Conversion from ProductDto to Product
  Product toProduct(ProductDto productDto);


  // Conversion from Product to ProductDto
//  @Mapping(/*target = "imageUrls", source = "imageUrls"*/)
  ProductDto toResponseDto(Product product);

  // Existing mappings for internal DTOs
  Product toProduct(ProductCreateRequestDto createRequestDto);
  Product toProduct(ProductUpdateRequestDto updateRequestDto);
  ProductResponseDto toResponseDto(Product product, @MappingTarget ProductResponseDto responseDto);
}
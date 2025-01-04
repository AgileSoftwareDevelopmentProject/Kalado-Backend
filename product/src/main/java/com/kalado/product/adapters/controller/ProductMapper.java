package com.kalado.product.adapters.controller;

import com.kalado.common.dto.ProductDto;
import com.kalado.product.domain.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
  // Ensure sellerId is explicitly mapped
  @Mapping(target = "sellerId", source = "sellerId")
  Product toProduct(ProductDto productDto);

  @Mapping(target = "sellerId", source = "sellerId")
  ProductDto toResponseDto(Product product);

  void updateProductFromDto(ProductDto productDto, @MappingTarget Product product);
}
package com.kalado.product.application.service;

import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.product.domain.model.Product;
import com.kalado.common.enums.ProductStatus;
import com.kalado.product.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
  private final ProductRepository productRepository;
  private final ImageService imageService;

  private static final int MAX_IMAGES = 3;
  private static final long MAX_IMAGE_SIZE = 1024 * 1024; // 1MB

  public Product createProduct(Product product, List<MultipartFile> images) {
    validateProduct(product);
    validateImages(images);

    // Process and store images
//    List<String> imageUrls = images.stream().map(imageService::storeImage).toList();

//    product.setImageUrls(imageUrls);
    product.setStatus(ProductStatus.ACTIVE);

    log.info("Creating new product: {}", product.getTitle());
    return productRepository.save(product);
  }

  @Transactional
  public Product updateProduct(Long id, Product updatedProduct, List<MultipartFile> newImages) {
    Product existingProduct = getProduct(id);
    validateProductOwnership(existingProduct, updatedProduct.getSellerId());

    if (newImages != null && !newImages.isEmpty()) {
      // Delete old images
//      existingProduct.getImageUrls().forEach(imageService::deleteImage);

      // Store new images
      List<String> imageUrls =
          newImages.stream().map(imageService::storeImage).collect(Collectors.toList());
//      existingProduct.setImageUrls(imageUrls);
    }

    updateProductFields(existingProduct, updatedProduct);
    return productRepository.save(existingProduct);
  }

  @Transactional
  public void deleteProduct(Long id, Long sellerId) {
    Product product = getProduct(id);
    validateProductOwnership(product, sellerId);

//    product.getImageUrls().forEach(imageService::deleteImage);

    product.setStatus(ProductStatus.DELETED);
    productRepository.save(product);
  }

  public Product updateProductStatus(Long id, ProductStatus newStatus, Long sellerId) {
    Product product = getProduct(id);
    validateProductOwnership(product, sellerId);

    product.setStatus(newStatus);
    Product updatedProduct = productRepository.save(product);
    log.info("Product status updated to {}: {}", newStatus, id);
    return updatedProduct;
  }

  public Product getProduct(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Product not found"));
  }

  @Transactional(readOnly = true) // Add for read operations
  public List<Product> getProductsBySeller(Long sellerId) {
    return productRepository.findBySellerId(sellerId);
  }

  @Transactional(readOnly = true)
  public List<Product> getProductsByCategory(String category) {
    return productRepository.findByCategory(category);
  }

  private void validateProduct(Product product) {
    if (product.getTitle() == null || product.getTitle().trim().isEmpty()) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Title is required");
    }
    if (product.getDescription() == null || product.getDescription().trim().isEmpty()) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Description is required");
    }
    if (product.getPrice() == null || product.getPrice().getAmount() <= 0) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Valid price is required");
    }
    if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Category is required");
    }
  }

  private void validateImages(List<MultipartFile> images) {
    if (images == null) return;

    if (images.size() > MAX_IMAGES) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Maximum " + MAX_IMAGES + " images allowed");
    }

    for (MultipartFile image : images) {
      if (image.getSize() > MAX_IMAGE_SIZE) {
        throw new CustomException(ErrorCode.BAD_REQUEST, "Image size must be less than 1MB");
      }
      if (!Objects.requireNonNull(image.getContentType()).startsWith("image/")) {
        throw new CustomException(ErrorCode.BAD_REQUEST, "File must be an image");
      }
    }
  }

  private void validateProductOwnership(Product product, Long sellerId) {
    if (!product.getSellerId().equals(sellerId)) {
      throw new CustomException(
          ErrorCode.FORBIDDEN, "You don't have permission to modify this product");
    }
  }

  private void updateProductFields(Product existingProduct, Product updatedProduct) {
    existingProduct.setTitle(updatedProduct.getTitle());
    existingProduct.setDescription(updatedProduct.getDescription());
    existingProduct.setPrice(updatedProduct.getPrice());
    existingProduct.setCategory(updatedProduct.getCategory());
    existingProduct.setProductionYear(updatedProduct.getProductionYear());
    existingProduct.setBrand(updatedProduct.getBrand());
  }
}

package com.kalado.product;

import com.kalado.common.Price;
import com.kalado.common.enums.CurrencyUnit;
import com.kalado.common.exception.CustomException;
import com.kalado.product.application.service.ImageService;
import com.kalado.product.application.service.ProductService;
import com.kalado.product.domain.model.Product;
import com.kalado.common.enums.ProductStatus;
import com.kalado.product.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @MockBean
    private ImageService imageService;

    private Product testProduct;
    private MockMultipartFile testImage;

    @BeforeEach
    void setUp() {
        // Clear database
        productRepository.deleteAll();

        // Set up test image
//        testImage = new MockMultipartFile(
//                "image.jpg",
//                "image.jpg",
//                "image/jpeg",
//                "test image content".getBytes()
//        );

        // Set up test product
        testProduct = Product.builder()
                .title("Test Product")
                .description("Test Description")
                .price(new Price(100000, CurrencyUnit.TOMAN))
                .category("Electronics")
                .productionYear(2023)
                .brand("Test Brand")
                .sellerId(1L)
                .build();

        // Mock image service
//        when(imageService.storeImage(any())).thenReturn("test-image-url.jpg");
    }

    @Test
    void createProduct_Success() {
//        List<MultipartFile> images = List.of(testImage);

        Product savedProduct = productService.createProduct(testProduct, null);

        assertNotNull(savedProduct.getId());
        assertEquals(testProduct.getTitle(), savedProduct.getTitle());
        assertEquals(ProductStatus.ACTIVE, savedProduct.getStatus());
//        assertEquals(1, savedProduct.getImageUrls().size());
//        verify(imageService, times(1)).storeImage(any());
    }

//    @Test
//    void createProduct_WithInvalidData_ShouldThrowException() {
//        testProduct.setTitle("");  // Invalid title
//
//        assertThrows(CustomException.class, () ->
//                productService.createProduct(testProduct, List.of(testImage))
//        );
//    }

    @Test
    void updateProduct_Success() {
        // First create a product
        Product savedProduct = productService.createProduct(testProduct, null);

        // Update product
        savedProduct.setTitle("Updated Title");
        savedProduct.setDescription("Updated Description");

        Product updatedProduct = productService.updateProduct(
                savedProduct.getId(),
                savedProduct,
                null  // No new images
        );

        assertEquals("Updated Title", updatedProduct.getTitle());
        assertEquals("Updated Description", updatedProduct.getDescription());
//        assertEquals(
//                new ArrayList<>(savedProduct.getImageUrls()),
//                new ArrayList<>(updatedProduct.getImageUrls()),
//                "Image URLs should match"
//        );
    }

//    @Test
//    void updateProduct_WithNewImages_Success() {
//        // First create a product
//        Product savedProduct = productService.createProduct(testProduct, List.of(testImage));
//        reset(imageService); // Reset mock count after initial creation
//
//        // Create new test image
//        MockMultipartFile newImage = new MockMultipartFile(
//                "new-image.jpg",
//                "new-image.jpg",
//                "image/jpeg",
//                "new test image content".getBytes()
//        );
//
//        when(imageService.storeImage(any())).thenReturn("new-test-image-url.jpg");
//
//        Product updatedProduct = productService.updateProduct(
//                savedProduct.getId(),
//                savedProduct,
//                List.of(newImage)
//        );
//
//        verify(imageService).deleteImage(any());
//        verify(imageService).storeImage(any());
//        assertEquals(1, updatedProduct.getImageUrls().size());
//        assertTrue(updatedProduct.getImageUrls().contains("new-test-image-url.jpg"));
//    }

    @Test
    void updateProduct_WrongSeller_ShouldThrowException() {
        Product savedProduct = productService.createProduct(testProduct, null);

        Product productWithWrongSeller = savedProduct.toBuilder()
                .sellerId(999L)  // Different seller ID
                .build();

        assertThrows(CustomException.class, () ->
                productService.updateProduct(
                        savedProduct.getId(),
                        productWithWrongSeller,
                        null
                )
        );
    }

    @Test
    void deleteProduct_Success() {
        Product savedProduct = productService.createProduct(testProduct, null);

        productService.deleteProduct(savedProduct.getId(), savedProduct.getSellerId());

        Product deletedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertEquals(ProductStatus.DELETED, deletedProduct.getStatus());
//        verify(imageService, times(1)).deleteImage(any());
    }

    @Test
    void updateProductStatus_Success() {
        Product savedProduct = productService.createProduct(testProduct, null);

        productService.updateProductStatus(
                savedProduct.getId(),
                ProductStatus.RESERVED,
                savedProduct.getSellerId()
        );

        Product updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertEquals(ProductStatus.RESERVED, updatedProduct.getStatus());
    }

    @Test
    void getProductsBySeller_Success() {
        // Clear the repository before the test
        productRepository.deleteAll();

        // Explicitly set the seller ID for both products
        Long sellerId = 1L;

        // Create first product
        Product firstProduct = Product.builder()
                .title("First Product")
                .description("Test Description 1")
                .price(new Price(100000, CurrencyUnit.TOMAN))
                .category("Electronics")
                .productionYear(2023)
                .brand("Test Brand 1")
                .sellerId(sellerId)
                .build();
        productService.createProduct(firstProduct, null);

        // Create second product
        Product secondProduct = Product.builder()
                .title("Second Product")
                .description("Test Description 2")
                .price(new Price(200000, CurrencyUnit.TOMAN))
                .category("Electronics")
                .productionYear(2022)
                .brand("Test Brand 2")
                .sellerId(sellerId)
                .build();
        productService.createProduct(secondProduct, null);

        // Retrieve products for the seller
        List<Product> sellerProducts = productService.getProductsBySeller(sellerId);

        // Assert
        assertEquals(2, sellerProducts.size(), "Should retrieve 2 products for the seller");
        assertTrue(sellerProducts.stream().allMatch(p -> p.getSellerId().equals(sellerId)),
                "All retrieved products should belong to the same seller");
    }

    @Test
    void getProductsByCategory_Success() {
        // Explicitly clear the repository before the test
        productRepository.deleteAll();

        // Create electronics product with a clear category
        Product electronicsProduct = Product.builder()
                .title("Electronics Product")
                .description("Test Description")
                .price(new Price(100000, CurrencyUnit.TOMAN))
                .category("Electronics")
                .productionYear(2023)
                .brand("Test Brand")
                .sellerId(1L)
                .build();
        productService.createProduct(electronicsProduct, null);

        // Create clothing product with a clear category
        Product clothingProduct = Product.builder()
                .title("Clothing Product")
                .description("Test Description")
                .price(new Price(50000, CurrencyUnit.TOMAN))
                .category("Clothing")
                .productionYear(2022)
                .brand("Another Brand")
                .sellerId(1L)
                .build();
        productService.createProduct(clothingProduct, null);

        List<Product> electronicsProducts = productService.getProductsByCategory("Electronics");
        List<Product> clothingProducts = productService.getProductsByCategory("Clothing");

        assertEquals(1, electronicsProducts.size());
        assertEquals(1, clothingProducts.size());
        assertEquals("Electronics", electronicsProducts.get(0).getCategory());
        assertEquals("Clothing", clothingProducts.get(0).getCategory());
    }
}
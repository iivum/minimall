package com.minimall.service;

import com.minimall.model.Product;
import com.minimall.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    @Test
    void findAllActive_returnsActiveProducts() {
        Product p1 = new Product();
        p1.setName("Product 1");
        p1.setActive(true);
        Product p2 = new Product();
        p2.setName("Product 2");
        p2.setActive(true);
        when(productRepository.findByActiveTrue()).thenReturn(Arrays.asList(p1, p2));

        List<Product> result = productService.findAllActive();

        assertEquals(2, result.size());
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void findAllActive_withPageable_returnsPaginatedProducts() {
        Product product = new Product();
        product.setName("Test Product");
        product.setActive(true);
        Page<Product> page = new PageImpl<>(List.of(product));
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByActiveTrue(pageable)).thenReturn(page);

        Page<Product> result = productService.findAllActive(pageable);

        assertEquals(1, result.getTotalElements());
        verify(productRepository).findByActiveTrue(pageable);
    }

    @Test
    void findAll_returnsAllProducts() {
        Product product = new Product();
        product.setName("Test");
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> result = productService.findAll();

        assertEquals(1, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void searchByName_returnsMatchingProducts() {
        Product product = new Product();
        product.setName("Test Product");
        when(productRepository.findByNameContainingIgnoreCaseAndActiveTrue("Test")).thenReturn(List.of(product));

        List<Product> result = productService.searchByName("Test");

        assertEquals(1, result.size());
        verify(productRepository).findByNameContainingIgnoreCaseAndActiveTrue("Test");
    }

    @Test
    void searchByName_withPageable_returnsPaginatedResults() {
        Product product = new Product();
        product.setName("Test Product");
        Page<Product> page = new PageImpl<>(List.of(product));
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByNameContainingIgnoreCaseAndActiveTrue("Test", pageable)).thenReturn(page);

        Page<Product> result = productService.searchByName("Test", pageable);

        assertEquals(1, result.getTotalElements());
        verify(productRepository).findByNameContainingIgnoreCaseAndActiveTrue("Test", pageable);
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(productRepository.findById("not-found")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.findById("not-found"));
    }

    @Test
    void findById_returnsProduct_whenExists() {
        Product product = new Product();
        product.setId("product-1");
        product.setName("Test");
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        Product result = productService.findById("product-1");

        assertEquals("Test", result.getName());
    }

    @Test
    void findByIds_returnsProducts() {
        Product product = new Product();
        product.setId("product-1");
        when(productRepository.findByIdIn(List.of("product-1"))).thenReturn(List.of(product));

        List<Product> result = productService.findByIds(List.of("product-1"));

        assertEquals(1, result.size());
        verify(productRepository).findByIdIn(List.of("product-1"));
    }

    @Test
    void create_savesAndReturnsProduct() {
        Product product = new Product();
        product.setName("Test");
        product.setPrice(BigDecimal.valueOf(99.99));
        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.create(product);

        assertNotNull(result);
        verify(productRepository).save(product);
    }

    @Test
    void update_modifiesProductFields() {
        Product existing = new Product();
        existing.setId("product-1");
        existing.setName("Old Name");
        existing.setPrice(BigDecimal.valueOf(10));

        Product updated = new Product();
        updated.setName("New Name");
        updated.setDescription("New Description");
        updated.setPrice(BigDecimal.valueOf(20));
        updated.setStock(100);
        updated.setImageUrl("http://example.com/image.jpg");
        updated.setActive(false);

        when(productRepository.findById("product-1")).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        Product result = productService.update("product-1", updated);

        assertEquals("New Name", existing.getName());
        assertEquals("New Description", existing.getDescription());
        assertEquals(BigDecimal.valueOf(20), existing.getPrice());
        assertEquals(100, existing.getStock());
        assertEquals("http://example.com/image.jpg", existing.getImageUrl());
        assertFalse(existing.getActive());
    }

    @Test
    void delete_setsActiveToFalse() {
        Product product = new Product();
        product.setId("1");
        product.setActive(true);
        when(productRepository.findById("1")).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.delete("1");

        assertFalse(product.getActive());
        verify(productRepository).save(product);
    }

    @Test
    void activate_setsActiveToTrue() {
        Product product = new Product();
        product.setId("1");
        product.setActive(false);
        when(productRepository.findById("1")).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.activate("1");

        assertTrue(product.getActive());
        verify(productRepository).save(product);
    }

    @Test
    void deactivate_setsActiveToFalse() {
        Product product = new Product();
        product.setId("1");
        product.setActive(true);
        when(productRepository.findById("1")).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.deactivate("1");

        assertFalse(product.getActive());
        verify(productRepository).save(product);
    }
}

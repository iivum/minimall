package com.minimall.service;

import com.minimall.model.Product;
import com.minimall.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    void findById_throwsWhenNotFound() {
        when(productRepository.findById("not-found")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.findById("not-found"));
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
    void findAllActive_withPaging_returnsPage() {
        Product p1 = new Product();
        p1.setName("Product 1");
        p1.setActive(true);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(p1), pageable, 1);
        when(productRepository.findByActiveTrue(pageable)).thenReturn(page);

        Page<Product> result = productService.findAllActive(pageable);

        assertEquals(1, result.getTotalElements());
        verify(productRepository).findByActiveTrue(pageable);
    }

    @Test
    void searchByName_returnsMatchingProducts() {
        Product p1 = new Product();
        p1.setName("Test Product");
        p1.setActive(true);
        when(productRepository.findByNameContainingIgnoreCaseAndActiveTrue("Test"))
            .thenReturn(List.of(p1));

        List<Product> result = productService.searchByName("Test");

        assertEquals(1, result.size());
        verify(productRepository).findByNameContainingIgnoreCaseAndActiveTrue("Test");
    }

    @Test
    void searchByName_withPaging_returnsPage() {
        Product p1 = new Product();
        p1.setName("Test Product");
        p1.setActive(true);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(p1), pageable, 1);
        when(productRepository.findByNameContainingIgnoreCaseAndActiveTrue("Test", pageable))
            .thenReturn(page);

        Page<Product> result = productService.searchByName("Test", pageable);

        assertEquals(1, result.getTotalElements());
        verify(productRepository).findByNameContainingIgnoreCaseAndActiveTrue("Test", pageable);
    }

    @Test
    void findById_returnsProductWhenExists() {
        Product product = new Product();
        product.setId("1");
        product.setName("Test");
        when(productRepository.findById("1")).thenReturn(Optional.of(product));

        Product result = productService.findById("1");

        assertNotNull(result);
        assertEquals("1", result.getId());
    }

    @Test
    void update_updatesAndReturnsProduct() {
        Product existing = new Product();
        existing.setId("1");
        existing.setName("Old Name");
        existing.setActive(true);
        Product updated = new Product();
        updated.setName("New Name");
        updated.setPrice(BigDecimal.valueOf(199.99));
        updated.setDescription("New desc");
        updated.setStock(50);
        updated.setImageUrl("http://img.new");
        updated.setActive(true);
        when(productRepository.findById("1")).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        Product result = productService.update("1", updated);

        assertEquals("New Name", existing.getName());
        assertEquals(BigDecimal.valueOf(199.99), existing.getPrice());
        verify(productRepository).save(existing);
    }

    @Test
    void activate_setsActiveToTrue() {
        Product product = new Product();
        product.setId("1");
        product.setActive(false);
        when(productRepository.findById("1")).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.activate("1");

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

        Product result = productService.deactivate("1");

        assertFalse(product.getActive());
        verify(productRepository).save(product);
    }
}

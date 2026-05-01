package com.minimall.service;

import com.minimall.model.AnalyticsEvent;
import com.minimall.model.Product;
import com.minimall.repository.AnalyticsEventRepository;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock private ProductRepository productRepository;
    @Mock private AnalyticsEventRepository analyticsEventRepository;
    private ProductService productService;
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(analyticsEventRepository);
        productService = new ProductService(productRepository, analyticsService);
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
        when(analyticsEventRepository.save(any(AnalyticsEvent.class))).thenAnswer(i -> i.getArgument(0));

        Product result = productService.create(product);

        assertNotNull(result);
        verify(productRepository).save(product);
        verify(analyticsEventRepository).save(any(AnalyticsEvent.class));
    }

    @Test
    void delete_setsActiveToFalse() {
        Product product = new Product();
        product.setId("1");
        product.setActive(true);
        when(productRepository.findById("1")).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(analyticsEventRepository.save(any(AnalyticsEvent.class))).thenAnswer(i -> i.getArgument(0));

        productService.delete("1");

        assertFalse(product.getActive());
        verify(productRepository).save(product);
        verify(analyticsEventRepository).save(any(AnalyticsEvent.class));
    }
}

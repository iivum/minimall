package com.minimall.controller;

import com.minimall.config.JwtAuthenticationFilter;
import com.minimall.model.Product;
import com.minimall.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("getProducts returns paginated products")
    @WithMockUser
    void getProducts_returnsPaginatedProducts() throws Exception {
        Product product = new Product();
        product.setId("prod-001");
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(99.99));

        Page<Product> page = new PageImpl<>(List.of(product));
        when(productService.findAllActive(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("prod-001"))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    @DisplayName("getProduct returns product by id")
    @WithMockUser
    void getProduct_returnsProductById() throws Exception {
        Product product = new Product();
        product.setId("prod-002");
        product.setName("Single Product");
        product.setPrice(BigDecimal.valueOf(49.99));

        when(productService.findById("prod-002")).thenReturn(product);

        mockMvc.perform(get("/api/products/prod-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("prod-002"))
                .andExpect(jsonPath("$.name").value("Single Product"));
    }

    @Test
    @DisplayName("createProduct creates and returns product")
    @WithMockUser
    void createProduct_createsAndReturnsProduct() throws Exception {
        Product created = new Product();
        created.setId("prod-003");
        created.setName("New Product");
        created.setPrice(BigDecimal.valueOf(199.99));

        when(productService.create(any(Product.class))).thenReturn(created);

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Product\",\"price\":199.99}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("prod-003"))
                .andExpect(jsonPath("$.name").value("New Product"));
    }

    @Test
    @DisplayName("deleteProduct returns no content")
    @WithMockUser
    void deleteProduct_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/prod-001")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
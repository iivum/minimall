package com.minimall.controller;

import com.minimall.model.Product;
import com.minimall.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private com.minimall.service.JwtService jwtService;

    private Product createTestProduct(String id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("Test description");
        product.setPrice(BigDecimal.valueOf(99.99));
        product.setStock(100);
        product.setActive(true);
        return product;
    }

    @Test
    @WithMockUser
    void getAllProducts_returnsActiveProductList() throws Exception {
        Product product1 = createTestProduct("prod-1", "Product One");
        Product product2 = createTestProduct("prod-2", "Product Two");
        when(productService.findAllActive()).thenReturn(List.of(product1, product2));

        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("prod-1"))
            .andExpect(jsonPath("$[0].name").value("Product One"))
            .andExpect(jsonPath("$[1].id").value("prod-2"))
            .andExpect(jsonPath("$[1].name").value("Product Two"));
    }

    @Test
    @WithMockUser
    void getProduct_returnsProduct_whenExists() throws Exception {
        String productId = "prod-123";
        Product product = createTestProduct(productId, "Test Product");
        when(productService.findById(productId)).thenReturn(product);

        mockMvc.perform(get("/api/products/{id}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.name").value("Test Product"))
            .andExpect(jsonPath("$.price").value(99.99))
            .andExpect(jsonPath("$.stock").value(100));
    }

    @Test
    @WithMockUser
    void getProduct_returnsError_whenNotFound() throws Exception {
        String productId = "nonexistent";
        when(productService.findById(productId)).thenThrow(new RuntimeException("Product not found: " + productId));

        mockMvc.perform(get("/api/products/{id}", productId))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void searchProducts_returnsMatchingProducts() throws Exception {
        String searchName = "phone";
        Product product = createTestProduct("prod-1", "iPhone 15");
        when(productService.searchByName(searchName)).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products/search")
                .param("name", searchName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("prod-1"))
            .andExpect(jsonPath("$[0].name").value("iPhone 15"));
    }

    @Test
    @WithMockUser
    void searchProducts_returnsEmptyList_whenNoMatch() throws Exception {
        when(productService.searchByName("nonexistent")).thenReturn(List.of());

        mockMvc.perform(get("/api/products/search")
                .param("name", "nonexistent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser
    void createProduct_returnsCreatedProduct() throws Exception {
        String productId = "new-prod-123";
        Product product = createTestProduct(productId, "New Product");
        when(productService.create(any(Product.class))).thenReturn(product);

        String requestBody = """
            {
                "name": "New Product",
                "description": "New description",
                "price": 149.99,
                "stock": 50,
                "active": true
            }
            """;

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.name").value("New Product"));
    }

    @Test
    @WithMockUser
    void updateProduct_returnsUpdatedProduct() throws Exception {
        String productId = "prod-123";
        Product product = createTestProduct(productId, "Updated Product");
        when(productService.update(eq(productId), any(Product.class))).thenReturn(product);

        String requestBody = """
            {
                "name": "Updated Product",
                "description": "Updated description",
                "price": 199.99,
                "stock": 75,
                "active": true
            }
            """;

        mockMvc.perform(put("/api/products/{id}", productId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.name").value("Updated Product"));
    }

    @Test
    @WithMockUser
    void deleteProduct_returnsNoContent() throws Exception {
        String productId = "prod-123";

        mockMvc.perform(delete("/api/products/{id}", productId)
                .with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    void getAllProducts_returnsUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createProduct_returnsUnauthorized_whenNotAuthenticated() throws Exception {
        String requestBody = """
            {
                "name": "New Product",
                "description": "New description",
                "price": 149.99,
                "stock": 50,
                "active": true
            }
            """;

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized());
    }
}

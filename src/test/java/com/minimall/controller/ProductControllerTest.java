package com.minimall.controller;

import com.minimall.model.Product;
import com.minimall.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @Test
    @WithMockUser
    void getProducts_returnsPageOfProducts() throws Exception {
        Product product = new Product();
        product.setId("prod-1");
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(99.99));
        product.setStock(10);
        product.setActive(true);

        Page<Product> page = new PageImpl<>(List.of(product));
        when(productService.findAllActive(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    @WithMockUser
    void getProduct_returnsProductById() throws Exception {
        Product product = new Product();
        product.setId("prod-1");
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(99.99));
        product.setStock(10);
        product.setActive(true);

        when(productService.findById("prod-1")).thenReturn(product);

        mockMvc.perform(get("/api/products/prod-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test Product"))
            .andExpect(jsonPath("$.price").value(99.99));
    }

    @Test
    @WithMockUser
    void createProduct_returnsCreatedProduct() throws Exception {
        Product product = new Product();
        product.setId("prod-new");
        product.setName("New Product");
        product.setPrice(BigDecimal.valueOf(149.99));
        product.setStock(50);
        product.setActive(true);

        when(productService.create(any(Product.class))).thenReturn(product);

        String requestBody = """
            {
                "name": "New Product",
                "description": "A new product",
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
            .andExpect(jsonPath("$.name").value("New Product"))
            .andExpect(jsonPath("$.price").value(149.99));
    }

    @Test
    @WithMockUser
    void updateProduct_returnsUpdatedProduct() throws Exception {
        Product product = new Product();
        product.setId("prod-1");
        product.setName("Updated Product");
        product.setPrice(BigDecimal.valueOf(199.99));
        product.setStock(20);
        product.setActive(true);

        when(productService.update(eq("prod-1"), any(Product.class))).thenReturn(product);

        String requestBody = """
            {
                "name": "Updated Product",
                "description": "Updated description",
                "price": 199.99,
                "stock": 20,
                "active": true
            }
            """;

        mockMvc.perform(put("/api/products/prod-1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Product"))
            .andExpect(jsonPath("$.price").value(199.99));
    }

    @Test
    @WithMockUser
    void deleteProduct_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/prod-1")
                .with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void searchProducts_returnsMatchingProducts() throws Exception {
        Product product = new Product();
        product.setId("prod-1");
        product.setName("Laptop");
        product.setPrice(BigDecimal.valueOf(999.99));
        product.setStock(5);
        product.setActive(true);

        when(productService.searchByName("Laptop")).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products/search")
                .param("name", "Laptop"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    @WithMockUser
    void getAllProducts_returnsNonPaginatedList() throws Exception {
        Product product1 = new Product();
        product1.setId("prod-1");
        product1.setName("Product 1");
        product1.setPrice(BigDecimal.valueOf(50.00));
        product1.setStock(100);
        product1.setActive(true);

        Product product2 = new Product();
        product2.setId("prod-2");
        product2.setName("Product 2");
        product2.setPrice(BigDecimal.valueOf(75.00));
        product2.setStock(50);
        product2.setActive(true);

        when(productService.findAllActive()).thenReturn(List.of(product1, product2));

        mockMvc.perform(get("/api/products/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Product 1"))
            .andExpect(jsonPath("$[1].name").value("Product 2"));
    }
}
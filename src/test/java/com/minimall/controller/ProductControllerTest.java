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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
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

    private Product mockProduct(String id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(BigDecimal.valueOf(49.99));
        product.setActive(true);
        return product;
    }

    @Test
    @WithMockUser
    void getProducts_returnsPagedProducts() throws Exception {
        Page<Product> page = new PageImpl<>(List.of(mockProduct("1", "Product 1"), mockProduct("2", "Product 2")));
        when(productService.findAllActive(org.mockito.ArgumentMatchers.any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("1"))
                .andExpect(jsonPath("$.content[1].id").value("2"));
    }

    @Test
    @WithMockUser
    void getProducts_withSearch_returnsMatchingProducts() throws Exception {
        Page<Product> page = new PageImpl<>(List.of(mockProduct("1", "iPhone")));
        when(productService.searchByName(eq("iPhone"), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/products").param("search", "iPhone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("iPhone"));
    }

    @Test
    @WithMockUser
    void getAllProducts_returnsNonPaginatedList() throws Exception {
        when(productService.findAllActive()).thenReturn(List.of(mockProduct("1", "P1"), mockProduct("2", "P2")));

        mockMvc.perform(get("/api/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));
    }

    @Test
    @WithMockUser
    void getProduct_returnsProduct() throws Exception {
        when(productService.findById("prod-1")).thenReturn(mockProduct("prod-1", "TestProduct"));

        mockMvc.perform(get("/api/products/prod-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("prod-1"))
                .andExpect(jsonPath("$.name").value("TestProduct"));
    }

    @Test
    @WithMockUser
    void searchProducts_returnsMatchingList() throws Exception {
        when(productService.searchByName("search-term")).thenReturn(List.of(mockProduct("3", "Found")));

        mockMvc.perform(get("/api/products/search").param("name", "search-term"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("3"));
    }

    @Test
    @WithMockUser
    void createProduct_savesAndReturnsProduct() throws Exception {
        Product product = mockProduct("new-prod", "NewProduct");
        when(productService.create(org.mockito.ArgumentMatchers.any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"NewProduct\",\"price\":49.99,\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("new-prod"));
    }

    @Test
    @WithMockUser
    void updateProduct_updatesAndReturnsProduct() throws Exception {
        Product product = mockProduct("prod-1", "UpdatedProduct");
        when(productService.update(eq("prod-1"), org.mockito.ArgumentMatchers.any(Product.class)))
                .thenReturn(product);

        mockMvc.perform(put("/api/products/prod-1")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"UpdatedProduct\",\"price\":59.99}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedProduct"));
    }

    @Test
    @WithMockUser
    void deleteProduct_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/prod-1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
package com.minimall.controller;

import com.minimall.model.Product;
import com.minimall.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private Model model;

    private AdminProductController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminProductController(productService);
    }

    @Test
    void products_returnsProductsView() {
        String viewName = controller.products();
        assertEquals("admin/products", viewName);
    }

    @Test
    void newProduct_setsModelAttributes() {
        controller.newProduct(model);
        verify(model).addAttribute("isEdit", false);
        verify(model).addAttribute("productId", "");
    }

    @Test
    void productDetail_setsProductIdModelAttribute() {
        controller.productDetail("prod-1", model);
        verify(model).addAttribute("productId", "prod-1");
    }

    @Test
    void editProduct_returnsProductFormView() {
        Product product = new Product();
        product.setId("prod-1");
        product.setName("Test Product");

        when(productService.findById("prod-1")).thenReturn(product);

        String viewName = controller.editProduct("prod-1", model);

        assertEquals("admin/product-form", viewName);
        verify(model).addAttribute("product", product);
        verify(model).addAttribute("isEdit", true);
        verify(model).addAttribute("productId", "prod-1");
    }
}

package com.minimall.controller;

import com.minimall.model.Product;
import com.minimall.service.AnalyticsService;
import com.minimall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product", description = "Product management APIs")
public class ProductController {
    private final ProductService productService;
    private final AnalyticsService analyticsService;

    public ProductController(ProductService productService, AnalyticsService analyticsService) {
        this.productService = productService;
        this.analyticsService = analyticsService;
    }

    @GetMapping
    @Operation(summary = "Get all active products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.findAllActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        Product product = productService.findById(id);
        analyticsService.track("PRODUCT_VIEW", null, "PRODUCT", id, null);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        List<Product> products = productService.searchByName(name);
        for (Product p : products) {
            analyticsService.track("PRODUCT_SEARCH", null, "PRODUCT", p.getId(), null);
        }
        return ResponseEntity.ok(products);
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.create(product));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.update(id, product));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete (deactivate) a product")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.minimall.controller;

import com.minimall.model.Product;
import com.minimall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
@Tag(name = "AdminProduct", description = "Admin Product Management APIs")
public class AdminProductController {
    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String products() {
        return "admin/products";
    }

    @GetMapping("/new")
    public String newProduct(Model model) {
        model.addAttribute("isEdit", false);
        model.addAttribute("productId", "");
        return "admin/product-form";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable String id, Model model) {
        model.addAttribute("productId", id);
        return "admin/product-detail";
    }

    @GetMapping("/{id}/edit")
    public String editProduct(@PathVariable String id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("isEdit", true);
        model.addAttribute("productId", id);
        return "admin/product-form";
    }

    // REST API endpoints for frontend integration
    @GetMapping("/api/list")
    @ResponseBody
    @Operation(summary = "List all products (admin)")
    public ResponseEntity<List<Product>> listProducts() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    @Operation(summary = "Get product by ID (admin)")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping("/api")
    @ResponseBody
    @Operation(summary = "Create product (admin)")
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());
        product.setActive(request.active() != null ? request.active() : true);
        return ResponseEntity.ok(productService.create(product));
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    @Operation(summary = "Update product (admin)")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());
        product.setActive(request.active() != null ? request.active() : true);
        return ResponseEntity.ok(productService.update(id, product));
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    @Operation(summary = "Delete product (admin) - soft delete")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/{id}/activate")
    @ResponseBody
    @Operation(summary = "Activate product (admin)")
    public ResponseEntity<Product> activateProduct(@PathVariable String id) {
        return ResponseEntity.ok(productService.activate(id));
    }

    @PatchMapping("/api/{id}/deactivate")
    @ResponseBody
    @Operation(summary = "Deactivate product (admin)")
    public ResponseEntity<Product> deactivateProduct(@PathVariable String id) {
        return ResponseEntity.ok(productService.deactivate(id));
    }

    public record ProductRequest(String name, String description, java.math.BigDecimal price, Integer stock, String imageUrl, Boolean active) {}
}
package com.minimall.controller;

import com.minimall.dto.DtoMapper;
import com.minimall.dto.ProductDTO;
import com.minimall.dto.ProductResponseDTO;
import com.minimall.model.Product;
import com.minimall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product", description = "Product management APIs")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get all active products (paginated)")
    public ResponseEntity<Page<ProductResponseDTO>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort) {
        Sort sortOrder = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("price-asc".equals(sort)) sortOrder = Sort.by(Sort.Direction.ASC, "price");
        else if ("price-desc".equals(sort)) sortOrder = Sort.by(Sort.Direction.DESC, "price");
        else if ("stock-asc".equals(sort)) sortOrder = Sort.by(Sort.Direction.ASC, "stock");
        else if ("stock-desc".equals(sort)) sortOrder = Sort.by(Sort.Direction.DESC, "stock");
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(productService.searchByName(search.trim(), pageable).map(ProductResponseDTO::from));
        }
        return ResponseEntity.ok(productService.findAllActive(pageable).map(ProductResponseDTO::from));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all active products (non-paginated)")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.findAllActive().stream().map(ProductResponseDTO::from).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable String id) {
        return ResponseEntity.ok(ProductResponseDTO.from(productService.findById(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name (non-paginated)")
    public ResponseEntity<List<ProductResponseDTO>> searchProducts(@RequestParam String name) {
        return ResponseEntity.ok(productService.searchByName(name).stream().map(ProductResponseDTO::from).toList());
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody Product product) {
        return ResponseEntity.ok(ProductResponseDTO.from(productService.create(product)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable String id, @Valid @RequestBody Product product) {
        return ResponseEntity.ok(ProductResponseDTO.from(productService.update(id, product)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete (deactivate) a product")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
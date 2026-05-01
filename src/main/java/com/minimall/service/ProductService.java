package com.minimall.service;

import com.minimall.model.Product;
import com.minimall.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final AnalyticsService analyticsService;

    public ProductService(ProductRepository productRepository, AnalyticsService analyticsService) {
        this.productRepository = productRepository;
        this.analyticsService = analyticsService;
    }

    public List<Product> findAllActive() {
        return productRepository.findByActiveTrue();
    }

    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name);
    }

    public Product findById(String id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    public Product create(Product product) {
        Product saved = productRepository.save(product);
        analyticsService.track("PRODUCT_CREATE", null, "PRODUCT", saved.getId(), null);
        return saved;
    }

    public Product update(String id, Product updated) {
        Product existing = findById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setStock(updated.getStock());
        existing.setImageUrl(updated.getImageUrl());
        existing.setActive(updated.getActive());
        Product saved = productRepository.save(existing);
        analyticsService.track("PRODUCT_UPDATE", null, "PRODUCT", id, null);
        return saved;
    }

    public void delete(String id) {
        Product product = findById(id);
        product.setActive(false);
        productRepository.save(product);
        analyticsService.track("PRODUCT_DELETE", null, "PRODUCT", id, null);
    }
}
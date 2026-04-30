package com.minimall.service;

import com.minimall.model.Product;
import com.minimall.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CacheService cacheService;

    private static final String PRODUCT_CACHE_PREFIX = "product:";

    public ProductService(ProductRepository productRepository, CacheService cacheService) {
        this.productRepository = productRepository;
        this.cacheService = cacheService;
    }

    public List<Product> findAllActive() {
        return productRepository.findByActiveTrue();
    }

    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name);
    }

    public Product findById(String id) {
        String cacheKey = PRODUCT_CACHE_PREFIX + id;
        Optional<Product> cached = cacheService.get(cacheKey, Product.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        cacheService.set(cacheKey, product);
        return product;
    }

    public Product create(Product product) {
        return productRepository.save(product);
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
        cacheService.evict(PRODUCT_CACHE_PREFIX + id);
        return saved;
    }

    public void delete(String id) {
        Product product = findById(id);
        product.setActive(false);
        productRepository.save(product);
        cacheService.evict(PRODUCT_CACHE_PREFIX + id);
    }
}
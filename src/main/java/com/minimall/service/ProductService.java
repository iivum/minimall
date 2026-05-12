package com.minimall.service;

import com.minimall.config.CaffeineCacheConfig;
import com.minimall.model.Product;
import com.minimall.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Cacheable(value = CaffeineCacheConfig.PRODUCTS_CACHE, key = "'all_active'")
    public List<Product> findAllActive() {
        return productRepository.findByActiveTrue();
    }

    @Cacheable(value = CaffeineCacheConfig.PRODUCTS_CACHE, key = "'page_'+#pageable.pageNumber+'_'+#pageable.pageSize")
    public Page<Product> findAllActive(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Cacheable(value = CaffeineCacheConfig.PRODUCTS_CACHE, key = "'search_'+#name")
    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name);
    }

    @Cacheable(value = CaffeineCacheConfig.PRODUCTS_CACHE, key = "'search_page_'+#name+'_'+#pageable.pageNumber+'_'+#pageable.pageSize")
    public Page<Product> searchByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable);
    }

    @Cacheable(value = CaffeineCacheConfig.PRODUCTS_CACHE, key = "#id")
    public Product findById(String id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    @CacheEvict(value = CaffeineCacheConfig.PRODUCTS_CACHE, allEntries = true)
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @CacheEvict(value = CaffeineCacheConfig.PRODUCTS_CACHE, allEntries = true)
    public Product update(String id, Product updated) {
        Product existing = findById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setStock(updated.getStock());
        existing.setImageUrl(updated.getImageUrl());
        existing.setActive(updated.getActive());
        return productRepository.save(existing);
    }

    @CacheEvict(value = CaffeineCacheConfig.PRODUCTS_CACHE, allEntries = true)
    public void delete(String id) {
        Product product = findById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @CacheEvict(value = CaffeineCacheConfig.PRODUCTS_CACHE, allEntries = true)
    public Product activate(String id) {
        Product product = findById(id);
        product.setActive(true);
        return productRepository.save(product);
    }

    @CacheEvict(value = CaffeineCacheConfig.PRODUCTS_CACHE, allEntries = true)
    public Product deactivate(String id) {
        Product product = findById(id);
        product.setActive(false);
        return productRepository.save(product);
    }
}
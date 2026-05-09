package com.minimall.service;

import com.minimall.model.Product;
import com.minimall.repository.ProductRepository;
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

    public List<Product> findAllActive() {
        return productRepository.findByActiveTrue();
    }

    public Page<Product> findAllActive(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name);
    }

    public Page<Product> searchByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable);
    }

    public Product findById(String id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found: " + id));
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
        return productRepository.save(existing);
    }

    public void delete(String id) {
        Product product = findById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    public Product activate(String id) {
        Product product = findById(id);
        product.setActive(true);
        return productRepository.save(product);
    }

    public Product deactivate(String id) {
        Product product = findById(id);
        product.setActive(false);
        return productRepository.save(product);
    }
}
package com.minimall.dto;

import com.minimall.model.Product;
import java.math.BigDecimal;

public record ProductResponseDTO(
    String id,
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    String imageUrl,
    Boolean active
) {
    public static ProductResponseDTO from(Product product) {
        return new ProductResponseDTO(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getImageUrl(),
            product.getActive()
        );
    }
}
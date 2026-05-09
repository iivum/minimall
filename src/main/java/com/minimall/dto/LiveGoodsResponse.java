package com.minimall.dto;

import com.minimall.model.LiveGoods;
import java.math.BigDecimal;

public record LiveGoodsResponse(
    String id,
    String productId,
    String name,
    String imageUrl,
    BigDecimal price,
    BigDecimal originalPrice,
    Integer stock,
    Integer salesCount,
    Integer sortOrder
) {
    public static LiveGoodsResponse from(LiveGoods goods) {
        return new LiveGoodsResponse(
            goods.getId(),
            goods.getProductId(),
            goods.getName(),
            goods.getImageUrl(),
            goods.getPrice(),
            goods.getOriginalPrice(),
            goods.getStock(),
            goods.getSalesCount(),
            goods.getSortOrder()
        );
    }
}
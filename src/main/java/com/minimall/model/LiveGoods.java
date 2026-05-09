package com.minimall.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "live_goods")
public class LiveGoods {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_room_id", nullable = false)
    private LiveRoom liveRoom;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(nullable = false)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "sales_count", nullable = false)
    private Integer salesCount = 0;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LiveRoom getLiveRoom() { return liveRoom; }
    public void setLiveRoom(LiveRoom liveRoom) { this.liveRoom = liveRoom; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getSalesCount() { return salesCount; }
    public void setSalesCount(Integer salesCount) { this.salesCount = salesCount; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
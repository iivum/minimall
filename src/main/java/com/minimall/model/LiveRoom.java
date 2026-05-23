package com.minimall.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "live_rooms", indexes = {
    @Index(name = "idx_live_rooms_status", columnList = "status")
})
public class LiveRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "stream_url")
    private String streamUrl;

    @Column(name = "anchor_name")
    private String anchorName;

    @Column(name = "anchor_avatar")
    private String anchorAvatar;

    @Column(nullable = false)
    private Integer viewerCount = 0;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "goods_count", nullable = false)
    private Integer goodsCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LiveStatus status = LiveStatus.PENDING;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "liveRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LiveGoods> goods = new ArrayList<>();

    public enum LiveStatus {
        PENDING, LIVE, ENDED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getStreamUrl() { return streamUrl; }
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }
    public String getAnchorName() { return anchorName; }
    public void setAnchorName(String anchorName) { this.anchorName = anchorName; }
    public String getAnchorAvatar() { return anchorAvatar; }
    public void setAnchorAvatar(String anchorAvatar) { this.anchorAvatar = anchorAvatar; }
    public Integer getViewerCount() { return viewerCount; }
    public void setViewerCount(Integer viewerCount) { this.viewerCount = viewerCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getGoodsCount() { return goodsCount; }
    public void setGoodsCount(Integer goodsCount) { this.goodsCount = goodsCount; }
    public LiveStatus getStatus() { return status; }
    public void setStatus(LiveStatus status) { this.status = status; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public Instant getCreatedAt() { return createdAt; }
    public List<LiveGoods> getGoods() { return goods; }
    public void setGoods(List<LiveGoods> goods) { this.goods = goods; }
}
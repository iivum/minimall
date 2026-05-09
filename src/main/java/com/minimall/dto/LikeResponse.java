package com.minimall.dto;

public record LikeResponse(
    boolean liked,
    Integer likeCount
) {}
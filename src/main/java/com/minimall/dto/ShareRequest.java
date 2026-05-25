package com.minimall.dto;

import jakarta.validation.constraints.NotBlank;

public record ShareRequest(
    @NotBlank String productId,
    @NotBlank String channel
) {}

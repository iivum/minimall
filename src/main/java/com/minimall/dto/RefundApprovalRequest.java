package com.minimall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefundApprovalRequest(
    @NotNull(message = "Approved status is required")
    Boolean approved,

    @NotBlank(message = "Admin comment is required")
    String adminComment
) {}
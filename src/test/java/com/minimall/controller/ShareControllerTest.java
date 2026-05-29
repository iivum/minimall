package com.minimall.controller;

import com.minimall.dto.ShareRequest;
import com.minimall.dto.ShareResponse;
import com.minimall.model.ShareReward;
import com.minimall.service.ShareService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShareController.class)
class ShareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShareService shareService;

    @MockBean
    private com.minimall.service.JwtService jwtService;

    private ShareReward createShareReward(String id, String userId, String productId) {
        ShareReward reward = new ShareReward();
        reward.setId(id);
        reward.setRewardType(ShareReward.RewardType.CASH);
        reward.setRewardAmount(BigDecimal.valueOf(5));
        return reward;
    }

    @Test
    @WithMockUser
    void createShareLink_returnsShareResponse() throws Exception {
        ShareResponse response = new ShareResponse(
            "share-abc123", "https://minimall.com/share/share-abc123",
            "https://minimall.com/posters/abc123.png", Instant.now().plusSeconds(604800)
        );
        when(shareService.createShareLink(eq("user-1"), any(ShareRequest.class))).thenReturn(response);

        String requestBody = """
            {"productId": "prod-1", "channel": "WECHAT"}
            """;

        mockMvc.perform(post("/api/share")
                .with(csrf())
                .header("X-User-Id", "user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shareUrl").value("https://minimall.com/share/share-abc123"));
    }

    @Test
    @WithMockUser
    void getUserRewards_returnsPaginatedRewards() throws Exception {
        ShareReward reward = createShareReward("reward-1", "user-1", "prod-1");

        Page<ShareReward> page = new PageImpl<>(List.of(reward));
        when(shareService.getUserRewards(eq("user-1"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/share/rewards")
                .header("X-User-Id", "user-1")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getUserRewardsAll_returnsAllRewards() throws Exception {
        ShareReward reward = createShareReward("reward-1", "user-1", "prod-1");

        when(shareService.getUserRewards("user-1")).thenReturn(List.of(reward));

        mockMvc.perform(get("/api/share/rewards/all")
                .header("X-User-Id", "user-1"))
            .andExpect(status().isOk());
    }
}
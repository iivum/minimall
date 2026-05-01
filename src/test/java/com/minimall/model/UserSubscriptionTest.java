package com.minimall.model;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class UserSubscriptionTest {
    @Test
    void testDefaultValues() {
        UserSubscription sub = new UserSubscription();
        assertFalse(sub.isOrderCreatedEnabled());
        assertFalse(sub.isOrderPaidEnabled());
        assertFalse(sub.isOrderShippedEnabled());
        assertFalse(sub.isOrderCompletedEnabled());
    }

    @Test
    void testSettersAndGetters() {
        UserSubscription sub = new UserSubscription();
        sub.setOpenid("test_openid");
        sub.setOrderCreatedEnabled(true);
        assertEquals("test_openid", sub.getOpenid());
        assertTrue(sub.isOrderCreatedEnabled());
    }
}
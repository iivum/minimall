package com.minimall.model;

import org.junit.jupiter.api.Test;
import java.util.UUID;
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
        sub.setOpenid("test_openid_" + UUID.randomUUID().toString().substring(0, 8));
        sub.setOrderCreatedEnabled(true);
        assertTrue(sub.getOpenid().startsWith("test_openid_"));
        assertTrue(sub.isOrderCreatedEnabled());
    }
}
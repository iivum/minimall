package com.minimall.service;

import com.minimall.dto.RefundApplicationRequest;
import com.minimall.dto.RefundApprovalRequest;
import com.minimall.dto.RefundResponse;
import com.minimall.model.Order;
import com.minimall.model.RefundRequest;
import com.minimall.model.User;
import com.minimall.repository.RefundRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private RefundRequestRepository refundRequestRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @Mock
    private PayService payService;

    @Mock
    private WeChatSubscribeService subscribeService;

    private RefundService refundService;

    @BeforeEach
    void setUp() {
        refundService = new RefundService(
            refundRequestRepository,
            orderService,
            userService,
            payService,
            subscribeService
        );
    }

    @Test
    @DisplayName("applyForRefund throws when order not paid")
    void applyForRefund_throwsWhenOrderNotPaid() {
        Order order = new Order();
        order.setId("order-123");
        order.setPayStatus(Order.PayStatus.UNPAID);

        when(orderService.findById("order-123")).thenReturn(order);

        RefundApplicationRequest request = new RefundApplicationRequest(
            BigDecimal.valueOf(100),
            "Reason"
        );

        assertThatThrownBy(() -> refundService.applyForRefund("order-123", "user-123", request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Only paid orders can be refunded");
    }

    @Test
    @DisplayName("applyForRefund throws when user does not own order")
    void applyForRefund_throwsWhenUserDoesNotOwnOrder() {
        User owner = new User();
        owner.setId("user-owner");

        Order order = new Order();
        order.setId("order-123");
        order.setPayStatus(Order.PayStatus.PAID);
        order.setUser(owner);

        when(orderService.findById("order-123")).thenReturn(order);

        RefundApplicationRequest request = new RefundApplicationRequest(
            BigDecimal.valueOf(100),
            "Reason"
        );

        assertThatThrownBy(() -> refundService.applyForRefund("order-123", "different-user", request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("your own orders");
    }

    @Test
    @DisplayName("applyForRefund throws when amount exceeds order total")
    void applyForRefund_throwsWhenAmountExceedsTotal() {
        User owner = new User();
        owner.setId("user-123");

        Order order = new Order();
        order.setId("order-123");
        order.setPayStatus(Order.PayStatus.PAID);
        order.setUser(owner);
        order.setTotalAmount(BigDecimal.valueOf(50));

        when(orderService.findById("order-123")).thenReturn(order);

        RefundApplicationRequest request = new RefundApplicationRequest(
            BigDecimal.valueOf(100),
            "Reason"
        );

        assertThatThrownBy(() -> refundService.applyForRefund("order-123", "user-123", request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("cannot exceed order total amount");
    }

    @Test
    @DisplayName("approveRefund throws when user is not admin")
    void approveRefund_throwsWhenUserNotAdmin() {
        User user = new User();
        user.setId("user-123");
        user.setRole(User.Role.USER);

        RefundRequest refund = new RefundRequest();
        refund.setId("refund-123");
        refund.setStatus(RefundRequest.Status.PENDING);

        when(refundRequestRepository.findById("refund-123")).thenReturn(Optional.of(refund));
        when(userService.findById("user-123")).thenReturn(user);

        RefundApprovalRequest request = new RefundApprovalRequest(true, "Approved");

        assertThatThrownBy(() -> refundService.approveRefund("refund-123", "user-123", request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("administrators");
    }

    @Test
    @DisplayName("approveRefund throws when refund is not pending")
    void approveRefund_throwsWhenRefundNotPending() {
        RefundRequest refund = new RefundRequest();
        refund.setId("refund-123");
        refund.setStatus(RefundRequest.Status.APPROVED);

        when(refundRequestRepository.findById("refund-123")).thenReturn(Optional.of(refund));

        RefundApprovalRequest request = new RefundApprovalRequest(true, "Approved");

        assertThatThrownBy(() -> refundService.approveRefund("refund-123", "admin-123", request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Only pending refund requests");
    }
}
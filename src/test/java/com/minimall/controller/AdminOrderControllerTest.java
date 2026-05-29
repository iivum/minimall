package com.minimall.controller;

import com.minimall.model.Order;
import com.minimall.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminOrderControllerTest {

    @Mock
    private OrderService orderService;

    private AdminOrderController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminOrderController(orderService);
    }

    @Test
    void getAllOrders_returnsPaginatedOrders() {
        Order order = new Order();
        order.setId("order-1");
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderService.findAll(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<Order>> response = controller.getAllOrders(0, 10);

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getAllOrdersAll_returnsAllOrders() {
        Order order = new Order();
        order.setId("order-1");
        when(orderService.findAll()).thenReturn(List.of(order));

        ResponseEntity<List<Order>> response = controller.getAllOrdersAll();

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getOrder_returnsOrder_whenExists() {
        Order order = new Order();
        order.setId("order-1");
        when(orderService.findById("order-1")).thenReturn(order);

        ResponseEntity<Order> response = controller.getOrder("order-1");

        assertNotNull(response.getBody());
        assertEquals("order-1", response.getBody().getId());
    }

    @Test
    void getOrderByNo_returnsOrder_whenExists() {
        Order order = new Order();
        order.setOrderNo("ORD-001");
        when(orderService.findByOrderNo("ORD-001")).thenReturn(order);

        ResponseEntity<Order> response = controller.getOrderByNo("ORD-001");

        assertNotNull(response.getBody());
        assertEquals("ORD-001", response.getBody().getOrderNo());
    }

    @Test
    void updateStatus_returnsUpdatedOrder() {
        Order order = new Order();
        order.setId("order-1");
        order.setStatus(Order.Status.PENDING);
        when(orderService.updateStatus("order-1", Order.Status.PAID)).thenReturn(order);

        ResponseEntity<Order> response = controller.updateStatus("order-1", Order.Status.PAID);

        assertNotNull(response.getBody());
        assertEquals(Order.Status.PENDING, response.getBody().getStatus());
    }

    @Test
    void getAllOrders_usesCorrectPagination() {
        Page<Order> page = new PageImpl<>(Collections.emptyList());
        when(orderService.findAll(any(Pageable.class))).thenReturn(page);

        controller.getAllOrders(2, 20);

        verify(orderService).findAll(argThat(pageable ->
            pageable.getPageNumber() == 2 && pageable.getPageSize() == 20));
    }

    @Test
    void getOrder_returnsOrder_whenNotFound() {
        when(orderService.findById("not-found")).thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> controller.getOrder("not-found"));
    }
}

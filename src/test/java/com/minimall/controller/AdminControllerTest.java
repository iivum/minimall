package com.minimall.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Test
    void dashboard_returnsAdminDashboardView() {
        AdminController controller = new AdminController();

        String viewName = controller.dashboard();

        assertEquals("admin/dashboard", viewName);
    }
}
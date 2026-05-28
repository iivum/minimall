package com.minimall.e2e;

import com.minimall.MinimallApplication;
import com.minimall.config.E2ETestConfig;
import com.minimall.model.Product;
import com.minimall.model.User;
import com.minimall.repository.ProductRepository;
import com.minimall.repository.UserRepository;
import com.minimall.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MinimallApplication.class)
@AutoConfigureMockMvc
@Import(E2ETestConfig.class)
@ActiveProfiles("test")
class OrderFlowE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private String authToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByOpenid("testuser").orElseThrow();
        authToken = jwtService.generateToken(testUser.getId(), testUser.getOpenid());
    }

    @Test
    void createOrderFlow_withValidData_returnsSuccess() throws Exception {
        Product product = productRepository.findAll().get(0);

        String requestBody = """
            {
                "userId": "%s",
                "items": [{"productId": "%s", "productName": "%s", "quantity": 2, "price": 99.99}]
            }
            """.formatted(testUser.getId(), product.getId(), product.getName());

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void getOrderFlow_whenOrderExists_returnsOrder() throws Exception {
        Product product = productRepository.findAll().get(0);

        String requestBody = """
            {
                "userId": "%s",
                "items": [{"productId": "%s", "productName": "%s", "quantity": 1, "price": 49.99}]
            }
            """.formatted(testUser.getId(), product.getId(), product.getName());

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/user/" + testUser.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }

    @Test
    void listOrdersFlow_returnsOrderList() throws Exception {
        mockMvc.perform(get("/api/orders/user/" + testUser.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }
}
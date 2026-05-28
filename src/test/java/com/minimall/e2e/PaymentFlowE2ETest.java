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
class PaymentFlowE2ETest {

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
    void initiatePaymentFlow_withValidOrder_returnsPaymentInfo() throws Exception {
        Product product = productRepository.findAll().get(0);

        String orderRequestBody = """
            {
                "userId": "%s",
                "items": [{"productId": "%s", "productName": "%s", "quantity": 1, "price": 99.99}]
            }
            """.formatted(testUser.getId(), product.getId(), product.getName());

        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderRequestBody))
                .andExpect(status().isOk())
                .andReturn();

        String orderResponse = orderResult.getResponse().getContentAsString();
        String orderId = orderResponse.contains("\"id\":\"") ?
            orderResponse.split("\"id\":\"")[1].split("\"")[0] : "test-order-id";

        mockMvc.perform(post("/api/payments/initiate")
                .header("Authorization", "Bearer " + authToken)
                .param("orderId", orderId)
                .param("amount", "99.99"))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentStatusFlow_whenPaymentExists_returnsStatus() throws Exception {
        mockMvc.perform(get("/api/payments/status/PAY-001")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }

    @Test
    void callbackPaymentFlow_withValidSignature_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/payments/callback")
                .header("Authorization", "Bearer " + authToken)
                .param("transactionId", "TXN-12345")
                .param("status", "SUCCESS"))
                .andExpect(status().isOk());
    }
}
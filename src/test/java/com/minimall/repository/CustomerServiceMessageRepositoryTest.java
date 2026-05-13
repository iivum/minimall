package com.minimall.repository;

import com.minimall.model.CustomerServiceMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CustomerServiceMessageRepositoryTest {

    @Autowired
    private CustomerServiceMessageRepository repository;

    @Test
    void saveAndFindByOpenid() {
        String uniqueOpenid = "test_openid_" + UUID.randomUUID().toString().substring(0, 8);
        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.setOpenid(uniqueOpenid);
        msg.setContent("Hello");
        msg.setFromCustomer(true);
        CustomerServiceMessage saved = repository.save(msg);
        assertNotNull(saved.getId());

        List<CustomerServiceMessage> messages = repository.findByOpenidOrderByCreatedAtDesc(uniqueOpenid);
        assertFalse(messages.isEmpty());
        assertEquals("Hello", messages.get(0).getContent());
    }

    @Test
    void findPendingMessages_returnsOnlyPending() {
        List<CustomerServiceMessage> pending = repository.findByStatus(CustomerServiceMessage.Status.PENDING);
        for (CustomerServiceMessage msg : pending) {
            assertEquals(CustomerServiceMessage.Status.PENDING, msg.getStatus());
        }
    }
}
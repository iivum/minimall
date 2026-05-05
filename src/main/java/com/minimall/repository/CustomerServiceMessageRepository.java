package com.minimall.repository;

import com.minimall.model.CustomerServiceMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerServiceMessageRepository extends JpaRepository<CustomerServiceMessage, String> {
    List<CustomerServiceMessage> findByOpenidOrderByCreatedAtDesc(String openid);
    List<CustomerServiceMessage> findByStatus(CustomerServiceMessage.Status status);
    List<CustomerServiceMessage> findByStatusAndHandlerIdIsNull(CustomerServiceMessage.Status status);
    long countByStatus(CustomerServiceMessage.Status status);
}
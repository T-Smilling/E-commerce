package com.javaweb.repository;

import com.javaweb.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findAllByEmail(String emailId);

    OrderEntity findByEmailAndId(String emailId, Long orderId);
}

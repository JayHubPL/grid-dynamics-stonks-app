package com.griddynamics.internship.stonksjh.order.repository;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.griddynamics.internship.stonksjh.order.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByUUID(UUID uuid);

}

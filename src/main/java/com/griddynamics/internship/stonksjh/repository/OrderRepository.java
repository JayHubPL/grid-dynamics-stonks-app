package com.griddynamics.internship.stonksjh.repository;

import com.griddynamics.internship.stonksjh.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
   
    Optional<Order> findByUuidAndOwnerUuid(UUID uuid, UUID ownerUuid);

    List<Order> findAllByOwnerUuid(UUID ownerUuid);

    List<Order> findAllByOwnerUuidAndSymbol(UUID ownerUuid, Order.Symbol symbol);

    List<Order> findAllByStatus(Order.Status status);

}

package com.griddynamics.internship.stonksjh.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.griddynamics.internship.stonksjh.dto.order.OrderResponseDTO;
import com.griddynamics.internship.stonksjh.exception.order.OrderNotFoundException;
import com.griddynamics.internship.stonksjh.exception.user.UserNotFoundException;
import com.griddynamics.internship.stonksjh.mapper.OrderMapper;
import com.griddynamics.internship.stonksjh.model.Order;
import com.griddynamics.internship.stonksjh.repository.OrderRepository;
import com.griddynamics.internship.stonksjh.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockTradingService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    public OrderResponseDTO readOneOrder(UUID ownerUuid, UUID orderUuid) {
        if (!userRepository.existsByUuid(ownerUuid)) {
            throw new UserNotFoundException(ownerUuid);
        }
        Order orderEntity = orderRepository.findByUuidAndOwnerUuid(orderUuid, ownerUuid)
                .orElseThrow(() -> new OrderNotFoundException(orderUuid));
        return orderMapper.entityToResponseDTO(orderEntity);
    }

    public List<OrderResponseDTO> readAllOrders(UUID ownerUuid) {
        if (!userRepository.existsByUuid(ownerUuid)) {
            throw new UserNotFoundException(ownerUuid);
        }
        return orderRepository.findAllByOwnerUuid(ownerUuid).stream()
                .map(orderMapper::entityToResponseDTO)
                .toList();
    }

}

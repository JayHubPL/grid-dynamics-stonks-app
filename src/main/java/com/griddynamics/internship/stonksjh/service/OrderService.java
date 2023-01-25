package com.griddynamics.internship.stonksjh.service;

import com.griddynamics.internship.stonksjh.dto.order.OrderRequestDTO;
import com.griddynamics.internship.stonksjh.dto.order.OrderResponseDTO;
import com.griddynamics.internship.stonksjh.exception.order.InvalidOrderTypeException;
import com.griddynamics.internship.stonksjh.exception.order.InvalidStockAmountException;
import com.griddynamics.internship.stonksjh.exception.order.InvalidSymbolException;
import com.griddynamics.internship.stonksjh.exception.order.OrderNotFoundException;
import com.griddynamics.internship.stonksjh.exception.user.UserNotFoundException;
import com.griddynamics.internship.stonksjh.mapper.OrderMapper;
import com.griddynamics.internship.stonksjh.model.Order;
import com.griddynamics.internship.stonksjh.model.User;
import com.griddynamics.internship.stonksjh.repository.OrderRepository;
import com.griddynamics.internship.stonksjh.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    public OrderResponseDTO create(UUID ownerUuid, OrderRequestDTO orderRequestDTO) {
        validateIfOwnerExists(ownerUuid);
        validateRequestDTO(orderRequestDTO);
        Order orderEntity = orderMapper.requestDtoToEntity(orderRequestDTO);
        User owner = userRepository.findByUuid(ownerUuid)
                .orElseThrow(() -> new UserNotFoundException(ownerUuid));
        orderEntity.setUuid(UUID.randomUUID());
        orderEntity.setOwner(owner);
        orderEntity = orderRepository.save(orderEntity);
        return orderMapper.entityToResponseDTO(orderEntity);
    }

    public OrderResponseDTO read(UUID ownerUuid, UUID orderUuid) {
        validateIfOwnerExists(ownerUuid);
        Order orderEntity = orderRepository.findByUuidAndOwnerUuid(orderUuid, ownerUuid)
                .orElseThrow(() -> new OrderNotFoundException(orderUuid));
        return orderMapper.entityToResponseDTO(orderEntity);
    }

    public List<OrderResponseDTO> read(UUID ownerUuid) {
        validateIfOwnerExists(ownerUuid);
        return orderRepository.findAllByOwnerUuid(ownerUuid).stream()
                .map(orderMapper::entityToResponseDTO)
                .toList();
    }

    public OrderResponseDTO update(UUID ownerUuid, UUID orderUuid, OrderRequestDTO orderRequestDTO) {
        validateIfOwnerExists(ownerUuid);
        Order orderEntity = orderRepository.findByUuidAndOwnerUuid(orderUuid, ownerUuid)
                .orElseThrow(() -> new OrderNotFoundException(orderUuid));
        validateRequestDTO(orderRequestDTO);
        orderEntity.setAmount(orderRequestDTO.amount());
        orderEntity.setSymbol(Order.Symbol.valueOf(orderRequestDTO.symbol()));
        orderEntity.setType(Order.Type.valueOf(orderRequestDTO.type()));
        orderEntity = orderRepository.save(orderEntity);
        return orderMapper.entityToResponseDTO(orderEntity);
    }

    public void delete(UUID ownerUuid, UUID orderUuid) {
        validateIfOwnerExists(ownerUuid);
        Order orderEntity = orderRepository.findByUuidAndOwnerUuid(orderUuid, ownerUuid)
                .orElseThrow(() -> new OrderNotFoundException(orderUuid));
        orderRepository.delete(orderEntity);
    }

    private void validateIfOwnerExists(UUID ownerUuid) {
        if (!userRepository.existsByUuid(ownerUuid)) {
            throw new UserNotFoundException(ownerUuid);
        }
    }

    private void validateRequestDTO(OrderRequestDTO orderRequestDTO) {
        validateAmount(orderRequestDTO.amount());
        validateSymbol(orderRequestDTO.symbol());
        validateOrderType(orderRequestDTO.type());
    }

    private void validateSymbol(String symbol) {
        try {
            Order.Symbol.valueOf(symbol);
        } catch (IllegalArgumentException ignored) {
            throw new InvalidSymbolException(symbol);
        }
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new InvalidStockAmountException(amount);
        }
    }

    private void validateOrderType(String type) {
        try {
            Order.Type.valueOf(type);
        } catch (IllegalArgumentException ignored) {
            throw new InvalidOrderTypeException(type);
        }
    }

}

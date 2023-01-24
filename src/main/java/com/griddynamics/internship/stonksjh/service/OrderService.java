package com.griddynamics.internship.stonksjh.service;

import com.griddynamics.internship.stonksjh.dto.order.OrderRequestDTO;
import com.griddynamics.internship.stonksjh.dto.order.OrderResponseDTO;
import com.griddynamics.internship.stonksjh.exception.order.InvalidOrderTypeException;
import com.griddynamics.internship.stonksjh.exception.order.InvalidStockAmountException;
import com.griddynamics.internship.stonksjh.exception.order.InvalidSymbolException;
import com.griddynamics.internship.stonksjh.exception.order.OrderNotFoundException;
import com.griddynamics.internship.stonksjh.mapper.OrderMapper;
import com.griddynamics.internship.stonksjh.model.Order;
import com.griddynamics.internship.stonksjh.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderResponseDTO create(OrderRequestDTO orderRequestDTO) {
        validateRequestDTO(orderRequestDTO);
        Order orderEntity = orderMapper.requestDtoToEntity(orderRequestDTO);
        orderEntity.setUuid(UUID.randomUUID());
        orderEntity = orderRepository.save(orderEntity);
        return orderMapper.entityToResponseDTO(orderEntity);
    }

    public OrderResponseDTO read(UUID uuid) {
        Order orderEntity = orderRepository.findByUUID(uuid)
                .orElseThrow(() -> new OrderNotFoundException(uuid));
        return orderMapper.entityToResponseDTO(orderEntity);
    }

    public OrderResponseDTO update(UUID uuid, OrderRequestDTO orderRequestDTO) {
        Order orderEntity = orderRepository.findByUUID(uuid)
                .orElseThrow(() -> new OrderNotFoundException(uuid));
        validateRequestDTO(orderRequestDTO);
        orderEntity.setAmount(orderRequestDTO.amount());
        orderEntity.setSymbol(Order.Symbol.valueOf(orderRequestDTO.symbol()));
        orderEntity.setType(Order.Type.valueOf(orderRequestDTO.type()));
        orderEntity = orderRepository.save(orderEntity);
        return orderMapper.entityToResponseDTO(orderEntity);
    }

    public void delete(UUID uuid) {
        Order orderEntity = orderRepository.findByUUID(uuid)
                .orElseThrow(() -> new OrderNotFoundException(uuid));
        orderRepository.delete(orderEntity);
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

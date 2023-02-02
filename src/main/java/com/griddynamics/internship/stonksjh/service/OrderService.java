package com.griddynamics.internship.stonksjh.service;

import com.griddynamics.internship.stonksjh.dto.order.OrderCreateRequestDTO;
import com.griddynamics.internship.stonksjh.dto.order.OrderResponseDTO;
import com.griddynamics.internship.stonksjh.dto.order.OrderUpdateRequestDTO;
import com.griddynamics.internship.stonksjh.exception.order.IllegalOrderOperationException;
import com.griddynamics.internship.stonksjh.exception.order.InvalidBidException;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    public OrderResponseDTO create(UUID ownerUuid, OrderCreateRequestDTO orderCreateRequestDTO) {
        validateIfOwnerExists(ownerUuid);
        validateCreateRequestDTO(orderCreateRequestDTO);
        Order orderEntity = orderMapper.createRequestDtoToEntity(orderCreateRequestDTO);
        User owner = userRepository.findByUuid(ownerUuid)
                .orElseThrow(() -> new UserNotFoundException(ownerUuid));
        orderEntity.setUuid(UUID.randomUUID());
        orderEntity.setStatus(Order.Status.PENDING);
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

    public OrderResponseDTO update(UUID ownerUuid, UUID orderUuid, OrderUpdateRequestDTO orderUpdateRequestDTO) {
        validateIfOwnerExists(ownerUuid);
        validateUpdateRequestDTO(orderUpdateRequestDTO);
        Order orderEntity = orderRepository.findByUuidAndOwnerUuid(orderUuid, ownerUuid)
                .orElseThrow(() -> new OrderNotFoundException(orderUuid));
        validateOrderStatus(orderEntity);
        orderEntity.setAmount(orderUpdateRequestDTO.amount());
        orderEntity.setSymbol(Order.Symbol.valueOf(orderUpdateRequestDTO.symbol()));
        orderEntity = orderRepository.save(orderEntity);
        return orderMapper.entityToResponseDTO(orderEntity);
    }

    public void delete(UUID ownerUuid, UUID orderUuid) {
        validateIfOwnerExists(ownerUuid);
        Order orderEntity = orderRepository.findByUuidAndOwnerUuid(orderUuid, ownerUuid)
                .orElseThrow(() -> new OrderNotFoundException(orderUuid));
        validateOrderStatus(orderEntity);
        orderRepository.delete(orderEntity);
    }

    private void validateOrderStatus(Order order) {
        if (order.getStatus() == Order.Status.COMPLETE) {
            throw new IllegalOrderOperationException(order.getUuid());
        }
    }

    private void validateUpdateRequestDTO(OrderUpdateRequestDTO orderUpdateRequestDTO) {
        validateAmount(orderUpdateRequestDTO.amount());
        validateSymbol(orderUpdateRequestDTO.symbol());
        validateBid(orderUpdateRequestDTO.bid());
    }

    private void validateIfOwnerExists(UUID ownerUuid) {
        if (!userRepository.existsByUuid(ownerUuid)) {
            throw new UserNotFoundException(ownerUuid);
        }
    }

    private void validateCreateRequestDTO(OrderCreateRequestDTO orderCreateRequestDTO) {
        validateAmount(orderCreateRequestDTO.amount());
        validateSymbol(orderCreateRequestDTO.symbol());
        validateOrderType(orderCreateRequestDTO.type());
        validateBid(orderCreateRequestDTO.bid());
    }

    private void validateBid(BigDecimal bid) {
        if (bid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBidException(bid);
        }
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

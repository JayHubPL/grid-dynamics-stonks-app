package com.griddynamics.internship.stonksjh.order.service;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.griddynamics.internship.stonksjh.order.dto.OrderDTO;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.InvalidStockAmountException;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.InvalidSymbolException;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.OrderNotFoundException;
import com.griddynamics.internship.stonksjh.order.mapper.OrderMapper;
import com.griddynamics.internship.stonksjh.order.model.Order;
import com.griddynamics.internship.stonksjh.order.model.Symbol;
import com.griddynamics.internship.stonksjh.order.repository.OrderRepository;

@Service
public class OrderCrudService {
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderMapper orderMapper;

    public OrderDTO createOrder(OrderDTO orderDTO) {
        validateAmount(orderDTO.getAmount());
        validateSymbol(orderDTO.getSymbol());
        orderDTO.setUuid(UUID.randomUUID());
        Order orderEntity = orderMapper.dtoToEntity(orderDTO);
        orderEntity = orderRepository.save(orderEntity);
        return orderMapper.entityToDto(orderEntity);
    }

    public OrderDTO readOneOrder(UUID uuid) {
        Order orderEntity = orderRepository.findByUUID(uuid)
            .orElseThrow(() -> new OrderNotFoundException(uuid));
        return orderMapper.entityToDto(orderEntity);
    }

    public OrderDTO updateOrder(UUID uuid, OrderDTO orderDTO) {
        validateAmount(orderDTO.getAmount());
        validateSymbol(orderDTO.getSymbol());
        Order orderEntity = orderRepository.findByUUID(uuid)
            .orElseThrow(() -> new OrderNotFoundException(uuid));
        orderEntity.setAmount(orderDTO.getAmount());
        orderEntity.setSymbol(orderDTO.getSymbol());
        orderEntity = orderRepository.save(orderEntity);
        return orderMapper.entityToDto(orderEntity);
    }

    public void deleteOrder(UUID uuid) {
        Order orderEntity = orderRepository.findByUUID(uuid)
            .orElseThrow(() -> new OrderNotFoundException(uuid));
        orderRepository.delete(orderEntity);
    }

    private void validateSymbol(String symbol) {
        boolean isValid = Arrays.stream(Symbol.values())
            .map(Symbol::toString)
            .filter(s -> s.equals(symbol))
            .count() == 1;
        if (!isValid) {
            throw new InvalidSymbolException(symbol);
        }
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new InvalidStockAmountException(amount);
        }
    }

}

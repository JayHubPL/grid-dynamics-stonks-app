package com.griddynamics.internship.stonksjh.order.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.griddynamics.internship.stonksjh.order.dto.OrderDTO;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.InvalidStockAmountException;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.OrderNotFoundException;
import com.griddynamics.internship.stonksjh.order.mapper.OrderMapper;
import com.griddynamics.internship.stonksjh.order.model.Order;
import com.griddynamics.internship.stonksjh.order.repository.OrderRepository;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;

    public OrderDTO createOrder(OrderDTO orderDTO) {
        validateAmount(orderDTO.getAmount());
        validateSymbol(orderDTO.getSymbol());
        orderDTO.setUuid(UUID.randomUUID());
        Order orderEntity = OrderMapper.INSTANCE.dtoToEntity(orderDTO);
        orderEntity = orderRepository.save(orderEntity);
        return OrderMapper.INSTANCE.entityToDto(orderEntity);
    }

    public OrderDTO readOneOrder(UUID uuid) {
        Order orderEntity = orderRepository.findByUUID(uuid)
            .orElseThrow(() -> new OrderNotFoundException(uuid));
        return OrderMapper.INSTANCE.entityToDto(orderEntity);
    }

    public OrderDTO updateOrder(UUID uuid, OrderDTO orderDTO) {
        validateAmount(orderDTO.getAmount());
        validateSymbol(orderDTO.getSymbol());
        Order orderEntity = orderRepository.findByUUID(uuid)
            .orElseThrow(() -> new OrderNotFoundException(uuid));
        orderEntity.setAmount(orderDTO.getAmount());
        orderEntity.setSymbol(orderDTO.getSymbol());
        orderEntity = orderRepository.save(orderEntity);
        return OrderMapper.INSTANCE.entityToDto(orderEntity);
    }

    public OrderDTO deleteOrder(UUID uuid) {
        Order orderEntity = orderRepository.findByUUID(uuid)
            .orElseThrow(() -> new OrderNotFoundException(uuid));
        orderRepository.delete(orderEntity);
        return OrderMapper.INSTANCE.entityToDto(orderEntity);
    }

    private void validateSymbol(String symbol) {
        // TODO should check if given symbol exists on finnhub.io
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new InvalidStockAmountException(amount);
        }
    }

}

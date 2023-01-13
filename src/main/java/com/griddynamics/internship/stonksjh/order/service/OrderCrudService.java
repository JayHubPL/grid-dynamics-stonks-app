package com.griddynamics.internship.stonksjh.order.service;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.griddynamics.internship.stonksjh.order.dto.CrudRequestDTO;
import com.griddynamics.internship.stonksjh.order.dto.OrderDTO;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.InvalidOrderTypeException;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.InvalidStockAmountException;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.InvalidSymbolException;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.OrderNotFoundException;
import com.griddynamics.internship.stonksjh.order.mapper.OrderMapper;
import com.griddynamics.internship.stonksjh.order.model.Order;
import com.griddynamics.internship.stonksjh.order.model.OrderType;
import com.griddynamics.internship.stonksjh.order.model.Symbol;
import com.griddynamics.internship.stonksjh.order.repository.OrderRepository;

@Service
public class OrderCrudService {
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderMapper orderMapper;

    public OrderDTO createOrder(CrudRequestDTO crudRequestDTO) {
        validateRequestDtoData(crudRequestDTO);
        Order orderEntity = orderMapper.requestDtoToEntity(crudRequestDTO);
        orderEntity.setUuid(UUID.randomUUID());
        orderEntity = orderRepository.save(orderEntity);
        return orderMapper.entityToDto(orderEntity);
    }

    public OrderDTO readOneOrder(UUID uuid) {
        Order orderEntity = orderRepository.findByUUID(uuid)
            .orElseThrow(() -> new OrderNotFoundException(uuid));
        return orderMapper.entityToDto(orderEntity);
    }

    public OrderDTO updateOrder(UUID uuid, CrudRequestDTO crudRequestDTO) {
        validateRequestDtoData(crudRequestDTO);
        Order orderEntity = orderRepository.findByUUID(uuid)
            .orElseThrow(() -> new OrderNotFoundException(uuid));
        orderEntity.setAmount(crudRequestDTO.getAmount());
        orderEntity.setSymbol(crudRequestDTO.getSymbol());
        orderEntity.setOrderType(OrderType.valueOf(crudRequestDTO.getOrderType()));
        orderEntity = orderRepository.save(orderEntity);
        return orderMapper.entityToDto(orderEntity);
    }

    public void deleteOrder(UUID uuid) {
        Order orderEntity = orderRepository.findByUUID(uuid)
            .orElseThrow(() -> new OrderNotFoundException(uuid));
        orderRepository.delete(orderEntity);
    }

    private void validateRequestDtoData(CrudRequestDTO crudRequestDTO) {
        validateAmount(crudRequestDTO.getAmount());
        validateSymbol(crudRequestDTO.getSymbol());
        validateOrderType(crudRequestDTO.getOrderType());
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

    private void validateOrderType(String orderType) {
        boolean isValid = Arrays.stream(OrderType.values())
            .map(OrderType::toString)
            .filter(s -> s.equals(orderType))
            .count() == 1;
        if (!isValid) {
            throw new InvalidOrderTypeException(orderType);
        }
    }

}

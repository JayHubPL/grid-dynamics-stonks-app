package com.griddynamics.internship.stonksjh.order.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.griddynamics.internship.stonksjh.order.dto.OrderDTO;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.InvalidStockAmountException;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.OrderNotFoundException;
import com.griddynamics.internship.stonksjh.order.mapper.OrderMapper;
import com.griddynamics.internship.stonksjh.order.model.Order;
import com.griddynamics.internship.stonksjh.order.repository.OrderRepository;

import lombok.val;

@RestController
@RequestMapping("api/orders")
public class OrderCrudController {
    
    @Autowired
    private OrderRepository orderRepository;

    @PostMapping(
        value = "/create",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> create(@RequestBody OrderDTO orderDTO) throws NoSuchMethodException {
        validateAmount(orderDTO.getAmount());
        validateSymbol(orderDTO.getSymbol());
        orderDTO.setUuid(UUID.randomUUID());
        orderRepository.save(OrderMapper.INSTANCE.dtoToEntity(orderDTO));
        return ResponseEntity.created(
            linkTo(OrderCrudController.class.getMethod("read", UUID.class), orderDTO.getUuid())
                .withSelfRel().toUri()
        ).build();
    }

    @GetMapping(
        value = "/get/{uuid}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OrderDTO> read(@PathVariable UUID uuid) throws NoSuchMethodException {
        val foundEntity = orderRepository.findByUUID(uuid).orElseThrow(() -> new OrderNotFoundException(uuid));
        val orderDto = OrderMapper.INSTANCE.entityToDto(foundEntity);
        val links = Set.of(
            linkTo(OrderCrudController.class.getMethod("read", UUID.class), uuid)
                .withSelfRel(),
            linkTo(OrderCrudController.class.getMethod("update", UUID.class, OrderDTO.class), uuid, orderDto)
                .withRel("update"),
            linkTo(OrderCrudController.class.getMethod("delete", UUID.class), uuid)
                .withRel("delete")
        );
        orderDto.add(links);
        return ResponseEntity.ok(orderDto);
    }

    @PostMapping(
        value = "/update/{uuid}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaTypes.HAL_JSON_VALUE
    )
    public ResponseEntity<Order> update(@PathVariable UUID uuid, @RequestBody OrderDTO orderDTO) {
        return null; // TODO
    }

    @DeleteMapping(
        value = "delete/{uuid}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Order> delete(@PathVariable UUID uuid) {
        return null; // TODO
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

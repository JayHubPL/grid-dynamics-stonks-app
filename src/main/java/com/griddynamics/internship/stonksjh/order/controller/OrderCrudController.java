package com.griddynamics.internship.stonksjh.order.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.griddynamics.internship.stonksjh.order.dto.OrderDTO;
import com.griddynamics.internship.stonksjh.order.service.OrderCrudService;

import lombok.val;

@RestController
@RequestMapping("api/orders")
public class OrderCrudController {
    
    @Autowired
    private OrderCrudService crudService;

    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> create(@RequestBody OrderDTO orderDTO) throws NoSuchMethodException {
        val createdOrderDTO = crudService.createOrder(orderDTO);
        return ResponseEntity.created(
            linkTo(OrderCrudController.class.getMethod("read", UUID.class), createdOrderDTO.getUuid())
                .withSelfRel().toUri()
        ).build();
    }

    @GetMapping(
        value = "/{uuid}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> read(@PathVariable UUID uuid) throws NoSuchMethodException {
        val readOrderDto = crudService.readOneOrder(uuid);
        return ResponseEntity.ok(readOrderDto);
    }

    @PutMapping(
        value = "/{uuid}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaTypes.HAL_JSON_VALUE
    )
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody OrderDTO orderDTO) throws NoSuchMethodException {
        val updatedOrderDTO = crudService.updateOrder(uuid, orderDTO);
        return ResponseEntity.ok(updatedOrderDTO);
    }

    @DeleteMapping(
        value = "/{uuid}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        val deletedEntity = crudService.deleteOrder(uuid);
        return ResponseEntity.ok(deletedEntity);
    }

}

package com.griddynamics.internship.stonksjh.controller;

import com.griddynamics.internship.stonksjh.dto.order.OrderRequestDTO;
import com.griddynamics.internship.stonksjh.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
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

import java.util.UUID;

@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService crudService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> create(@RequestBody OrderRequestDTO orderRequestDTO) {
        val orderDTO = crudService.create(orderRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDTO);
    }

    @GetMapping(
            value = "/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> read(@PathVariable UUID uuid) {
        val readOrderDto = crudService.read(uuid);
        return ResponseEntity.ok(readOrderDto);
    }

    @PutMapping(
            value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody OrderRequestDTO orderRequestDTO) {
        val updatedOrderDTO = crudService.update(uuid, orderRequestDTO);
        return ResponseEntity.ok(updatedOrderDTO);
    }

    @DeleteMapping(
            value = "/{uuid}"
    )
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        crudService.delete(uuid);
        return ResponseEntity.ok(null);
    }

}

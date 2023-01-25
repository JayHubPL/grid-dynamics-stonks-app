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
@RequestMapping("api/users/{userUuid}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService crudService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> create(@PathVariable UUID userUuid, @RequestBody OrderRequestDTO orderRequestDTO) {
        val orderDTO = crudService.create(userUuid, orderRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDTO);
    }

    @GetMapping(
            value = "{orderUuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> read(@PathVariable UUID userUuid, @PathVariable UUID orderUuid) {
        val readOrderDto = crudService.read(userUuid, orderUuid);
        return ResponseEntity.ok(readOrderDto);
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> read(@PathVariable UUID userUuid) {
        return ResponseEntity.ok(crudService.read(userUuid));
    }

    @PutMapping(
            value = "{orderUuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> update(@PathVariable UUID userUuid, @PathVariable UUID orderUuid,
            @RequestBody OrderRequestDTO orderRequestDTO) {
        val updatedOrderDTO = crudService.update(userUuid, orderUuid, orderRequestDTO);
        return ResponseEntity.ok(updatedOrderDTO);
    }

    @DeleteMapping(
            value = "{orderUuid}"
    )
    public ResponseEntity<?> delete(@PathVariable UUID userUuid, @PathVariable UUID orderUuid) {
        crudService.delete(userUuid, orderUuid);
        return ResponseEntity.ok(null);
    }

}

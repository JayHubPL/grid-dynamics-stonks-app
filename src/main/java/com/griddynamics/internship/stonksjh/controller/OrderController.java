package com.griddynamics.internship.stonksjh.controller;

import com.griddynamics.internship.stonksjh.dto.order.OrderCreateRequestDTO;
import com.griddynamics.internship.stonksjh.dto.order.OrderUpdateRequestDTO;
import com.griddynamics.internship.stonksjh.service.OrderService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<?> create(@PathVariable UUID userUuid, @RequestBody OrderCreateRequestDTO orderCreateRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crudService.create(userUuid, orderCreateRequestDTO));
    }

    @GetMapping(
            value = "{orderUuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> read(@PathVariable UUID userUuid, @PathVariable UUID orderUuid) {
        return ResponseEntity.ok(crudService.read(userUuid, orderUuid));
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
            @RequestBody OrderUpdateRequestDTO orderUpdateRequestDTO) {
        return ResponseEntity.ok(crudService.update(userUuid, orderUuid, orderUpdateRequestDTO));
    }

    @DeleteMapping(
            value = "{orderUuid}"
    )
    public ResponseEntity<?> delete(@PathVariable UUID userUuid, @PathVariable UUID orderUuid) {
        crudService.delete(userUuid, orderUuid);
        return ResponseEntity.ok(null);
    }

}

package com.griddynamics.internship.stonksjh.order.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.griddynamics.internship.stonksjh.order.dto.CrudRequestDTO;
import com.griddynamics.internship.stonksjh.order.service.OrderCrudService;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderCrudController {
    
    private final OrderCrudService crudService;

    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> create(@RequestBody CrudRequestDTO crudRequestDTO) {
        val orderDTO = crudService.createOrder(crudRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDTO);
    }

    @GetMapping(
        value = "/{uuid}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> read(@PathVariable UUID uuid) {
        val readOrderDto = crudService.readOneOrder(uuid);
        return ResponseEntity.ok(readOrderDto);
    }

    @PutMapping(
        value = "/{uuid}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody CrudRequestDTO crudRequestDTO)
    throws NoSuchMethodException {
        val updatedOrderDTO = crudService.updateOrder(uuid, crudRequestDTO);
        return ResponseEntity.ok(updatedOrderDTO);
    }

    @DeleteMapping(
        value = "/{uuid}"
    )
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        crudService.deleteOrder(uuid);
        return ResponseEntity.ok(null);
    }

}

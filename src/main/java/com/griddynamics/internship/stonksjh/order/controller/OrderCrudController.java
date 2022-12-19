package com.griddynamics.internship.stonksjh.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.griddynamics.internship.stonksjh.order.model.Order;
import com.griddynamics.internship.stonksjh.order.service.OrderService;

import lombok.val;

@RestController
public class OrderCrudController {
    
    @Autowired
    private OrderService orderService;

    @PostMapping(
        value = "/api/create",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Order> create(@RequestBody Order order) {
        return null; // TODO
    }

    @GetMapping(
        value = "/api/get/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Order> read(@PathVariable long id) {
        val opt = orderService.getOrderByID(id);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(opt.get());
    }

    @PostMapping(
        value = "/api/update/{id}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Order> update(@PathVariable long id) {
        return null; // TODO
    }

    @DeleteMapping(
        value = "/api/delete/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Order> delete() {
        return null; // TODO
    }

}

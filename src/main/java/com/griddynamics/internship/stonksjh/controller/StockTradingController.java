package com.griddynamics.internship.stonksjh.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.griddynamics.internship.stonksjh.service.StockTradingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/trading")
@RequiredArgsConstructor
public class StockTradingController {

    private final StockTradingService stockTradingService;
    
    @GetMapping(
            value = "/{userUuid}/orders/{orderUuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> readOneOrder(@PathVariable UUID userUuid, @PathVariable UUID orderUuid) {
        return ResponseEntity.ok(stockTradingService.readOneOrder(userUuid, orderUuid));
    }

    @GetMapping(
            value = "/{userUuid}/orders",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> readAllOrders(@PathVariable UUID userUuid) {
        return ResponseEntity.ok(stockTradingService.readAllOrders(userUuid));
    }

}

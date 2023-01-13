package com.griddynamics.internship.stonksjh.order.controller.util;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

public class TestDataFactory {
    
    public static Stream<Arguments> validOrderData() {
        return Stream.of(
            Arguments.of(10, "AAPL", "SELL"),
            Arguments.of(1, "MSFT", "BUY")
        );
    }

    public static Stream<Arguments> invalidOrderData() {
        return Stream.of(
            Arguments.of(0, "*", "XXX"),
            Arguments.of(-5, "", "")
        );
    }

}

package com.griddynamics.internship.stonksjh.util;

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
            Arguments.of(0, "AAPL", "SELL"),
            Arguments.of(1, "*", "BUY"),
            Arguments.of(1, "GOOG", "*"),
            Arguments.of(-5, "", "")
        );
    }

}

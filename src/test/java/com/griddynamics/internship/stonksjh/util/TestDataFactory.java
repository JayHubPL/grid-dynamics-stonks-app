package com.griddynamics.internship.stonksjh.util;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.griddynamics.internship.stonksjh.order.model.OrderType;
import com.griddynamics.internship.stonksjh.order.model.Symbol;

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

    public static Stream<Arguments> validStringStockSymbols() {
        return Arrays.stream(Symbol.values())
            .map(symbol -> Arguments.of(symbol.toString()));
    }

    public static Stream<Arguments> validStringOrderTypes() {
        return Arrays.stream(OrderType.values())
            .map(orderType -> Arguments.of(orderType.toString()));
    }

}

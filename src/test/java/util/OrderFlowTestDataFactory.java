package util;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public class OrderFlowTestDataFactory {

    public static Stream<Arguments> validOrderData() {
        return Stream.of(
                Arguments.of(10, "AAPL", "SELL"),
                Arguments.of(1, "MSFT", "BUY")
        );
    }

    public static Stream<Arguments> invalidAmounts() {
        return Stream.of(
                Arguments.of(-1),
                Arguments.of(Integer.MIN_VALUE)
        );
    }

    public static Stream<Arguments> invalidSymbolsOrTypes() {
        return Stream.of(
                Arguments.of("*"),
                Arguments.of(""),
                Arguments.of("\n\s\r")
        );
    }

}

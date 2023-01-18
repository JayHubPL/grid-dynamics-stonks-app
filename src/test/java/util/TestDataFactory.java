package util;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public class TestDataFactory {

    public static Stream<Arguments> validUserData() {
        return Stream.of(
                Arguments.of("user@example.com", "example"),
                Arguments.of("valid_email@with_.com", "user_name")
        );
    }

    public static Stream<Arguments> invalidUserData() {
        return Stream.of(
                Arguments.of("email@.com", "user"),
                Arguments.of("@email.com", "user"),
                Arguments.of("email.com", "user"),
                Arguments.of("illegal!@#chars.com", "user"),
                Arguments.of("", "user"),
                Arguments.of("e@mail", "user"),
                Arguments.of("valid@email.com", "illegal!@#chars")
        );
    }

    public static Stream<Arguments> validUserDataWithConflicts() {
        return Stream.of(
                Arguments.of("user@example.com", "different_username"),
                Arguments.of("different@email.com", "user")
        );
    }

}

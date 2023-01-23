package util;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public class UserFlowTestDataFactory {

    public static Stream<Arguments> validUserData() {
        return Stream.of(
                Arguments.of("user@example.com", "example"),
                Arguments.of("valid_email@with_.com", "user_name")
        );
    }

    public static Stream<Arguments> invalidEmails() {
        return Stream.of(
                Arguments.of("email@.com"),
                Arguments.of("@email.com"),
                Arguments.of("email.com"),
                Arguments.of("illegal!@#chars.com"),
                Arguments.of(""),
                Arguments.of("e@mail")
        );
    }

    public static Stream<Arguments> invalidUsernames() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("Abc1-"),
                Arguments.of("a b c"),
                Arguments.of("!@#$%^&*()-+=")
        );
    }

    public static Stream<Arguments> validUserDataWithEmailConflict() {
        return Stream.of(
                Arguments.of("user@example.com", "different_username")
        );
    }

    public static Stream<Arguments> validUserDataWithUsernameConflict() {
        return Stream.of(
                Arguments.of("different@email.com", "user")
        );
    }

}

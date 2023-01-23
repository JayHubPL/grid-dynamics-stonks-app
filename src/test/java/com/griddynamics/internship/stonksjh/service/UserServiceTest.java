package com.griddynamics.internship.stonksjh.service;

import com.griddynamics.internship.stonksjh.dto.user.UserRequestDTO;
import com.griddynamics.internship.stonksjh.dto.user.UserResponseDTO;
import com.griddynamics.internship.stonksjh.exception.user.EmailFormatException;
import com.griddynamics.internship.stonksjh.exception.user.EmailTakenException;
import com.griddynamics.internship.stonksjh.exception.user.UserNotFoundException;
import com.griddynamics.internship.stonksjh.exception.user.UsernameFormatException;
import com.griddynamics.internship.stonksjh.exception.user.UsernameTakenException;
import com.griddynamics.internship.stonksjh.mapper.UserMapper;
import com.griddynamics.internship.stonksjh.model.User;
import com.griddynamics.internship.stonksjh.repository.UserRepository;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({MockitoExtension.class, SpringExtension.class})
class UserServiceTest {

    private final UserMapper MAPPER = Mappers.getMapper(UserMapper.class);
    private final String PREDEFINED_UUID = "b16fbf04-09d5-4758-a6d6-a9200e5af2c9";
    private final String NONEXISTENT_UUID = "ab8f2253-d6c1-4adc-bd60-18e16916fb23";
    @MockBean
    private UserRepository USR_REPOSITORY;
    @MockBean
    private UserMapper INJECTED_MAPPER;
    private UserService CRUD_SERVICE;
    private UserResponseDTO PREDEFINED_USER;

    @BeforeAll
    void initUserService() {
        CRUD_SERVICE = new UserService(USR_REPOSITORY, INJECTED_MAPPER);
    }

    @BeforeAll
    void initPredefinedUser() {
        PREDEFINED_USER = UserResponseDTO.builder()
                .uuid(UUID.fromString(PREDEFINED_UUID))
                .email("user@example.com")
                .username("user")
                .balance(BigDecimal.ZERO)
                .build();
    }

    private UserResponseDTO toResponseDTO(UserRequestDTO userRequestDTO) {
        return UserResponseDTO.builder()
                .uuid(UUID.fromString(PREDEFINED_UUID))
                .email(userRequestDTO.email())
                .username(userRequestDTO.username())
                .balance(BigDecimal.ZERO)
                .build();
    }

    @Nested
    class Create {

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("UserService#create should not throw an exception when user data is correct")
        @MethodSource("util.UserFlowTestDataFactory#validUserData")
        void create_shouldNotThrow_whenUserDataOk(String email, String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username(username)
                    .build();

            when(INJECTED_MAPPER.requestDtoToEntity(userRequestDTO))
                    .thenReturn(MAPPER.requestDtoToEntity(userRequestDTO));

            assertDoesNotThrow(() -> CRUD_SERVICE.create(userRequestDTO));

            verify(INJECTED_MAPPER)
                    .requestDtoToEntity(userRequestDTO);
        }

        @ParameterizedTest(name = "{index}: email={0}")
        @DisplayName("UserService#create should throw EmailFormatException when email has illegal formatting")
        @MethodSource("util.UserFlowTestDataFactory#invalidEmails")
        void create_shouldThrow_whenEmailHasInvalidFormatting(String email) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username("valid_username")
                    .build();

            assertThatExceptionOfType(EmailFormatException.class)
                    .isThrownBy(() -> CRUD_SERVICE.create(userRequestDTO));
        }

        @ParameterizedTest(name = "{index}: username={0}")
        @DisplayName("UserService#create should throw UsernameFormatException when email has illegal formatting")
        @MethodSource("util.UserFlowTestDataFactory#invalidUsernames")
        void create_shouldThrow_whenUsernameHasInvalidFormatting(String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email("valid@email.com")
                    .username(username)
                    .build();

            assertThatExceptionOfType(UsernameFormatException.class)
                    .isThrownBy(() -> CRUD_SERVICE.create(userRequestDTO));
        }


        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("UserService#create should throw EmailTakenException when email is taken")
        @MethodSource("util.UserFlowTestDataFactory#validUserDataWithEmailConflict")
        void create_shouldThrow_whenEmailIsTaken(String email, String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username(username)
                    .build();

            when(USR_REPOSITORY.findByEmail(PREDEFINED_USER.email()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));
            when(USR_REPOSITORY.findByUsername(PREDEFINED_USER.username()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));

            assertThatExceptionOfType(EmailTakenException.class)
                    .isThrownBy(() -> CRUD_SERVICE.create(userRequestDTO));
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("UserService#create should throw UsernameTakenException when username is taken")
        @MethodSource("util.UserFlowTestDataFactory#validUserDataWithUsernameConflict")
        void create_shouldThrow_whenUsernameIsTaken(String email, String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username(username)
                    .build();

            when(USR_REPOSITORY.findByEmail(PREDEFINED_USER.email()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));
            when(USR_REPOSITORY.findByUsername(PREDEFINED_USER.username()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));

            assertThatExceptionOfType(UsernameTakenException.class)
                    .isThrownBy(() -> CRUD_SERVICE.create(userRequestDTO));
        }

    }

    @Nested
    class Read {

        @ParameterizedTest(name = "{index}: uuid={0}")
        @DisplayName("UserService#read should not throw an exception when user with given uuid exists")
        @ValueSource(strings = PREDEFINED_UUID)
        void readOne_shouldNotThrow_whenUserExists(String uuidStr) {
            val uuid = UUID.fromString(uuidStr);

            when(USR_REPOSITORY.findByUuid(uuid))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));
            when(INJECTED_MAPPER.entityToResponseDTO(any(User.class)))
                    .thenReturn(PREDEFINED_USER);

            assertDoesNotThrow(() -> CRUD_SERVICE.read(uuid));
            assertThat(CRUD_SERVICE.read(uuid))
                    .isEqualTo(PREDEFINED_USER);

            verify(USR_REPOSITORY, times(2))
                    .findByUuid(uuid);
            verify(INJECTED_MAPPER, times(2))
                    .entityToResponseDTO(any(User.class));
        }

        @ParameterizedTest(name = "{index}: uuid={0}")
        @DisplayName("UserService#read should throw UserNotFoundException when user with given uuid does not exist")
        @ValueSource(strings = NONEXISTENT_UUID)
        void readOne_shouldThrow_whenUserDoesNotExist(String uuidStr) {
            val uuid = UUID.fromString(uuidStr);

            when(USR_REPOSITORY.findByUuid(uuid))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(UserNotFoundException.class)
                    .isThrownBy(() -> CRUD_SERVICE.read(uuid));

            verify(USR_REPOSITORY)
                    .findByUuid(uuid);
        }

        @Test
        @DisplayName("UserService#read (no args overload) should return an empty list when no users exist")
        void readAll_shouldReturnEmptyList_whenNoUsersExist() {
            when(USR_REPOSITORY.findAll())
                    .thenReturn(List.of());

            assertThat(CRUD_SERVICE.read())
                    .isEmpty();

            verify(USR_REPOSITORY)
                    .findAll();
        }

        @Test
        @DisplayName("UserService#read (no args overload) should return a non-empty list when any users exist")
        void readAll_shouldReturnNonEmptyList_whenUsersExist() {
            when(USR_REPOSITORY.findAll())
                    .thenReturn(List.of(MAPPER.dtoToEntity(PREDEFINED_USER)));
            when(INJECTED_MAPPER.entityToResponseDTO(any(User.class)))
                    .thenReturn(PREDEFINED_USER);

            assertDoesNotThrow(() -> CRUD_SERVICE.read());
            val list = CRUD_SERVICE.read();
            assertThat(list)
                    .isNotEmpty();
            assertThat(list)
                    .isEqualTo(List.of(PREDEFINED_USER));

            verify(USR_REPOSITORY, times(2))
                    .findAll();
            verify(INJECTED_MAPPER, times(2))
                    .entityToResponseDTO(any(User.class));
        }

    }

    @Nested
    class Update {

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("UserService#update should return updated dto after updating existing user")
        @MethodSource("util.UserFlowTestDataFactory#validUserData")
        void update_shouldReturnCorrectDTO_whenUserDataOk(String email, String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username("valid_username")
                    .build();

            when(USR_REPOSITORY.findByUuid(PREDEFINED_USER.uuid()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));
            when(USR_REPOSITORY.findByEmail(PREDEFINED_USER.email()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));
            when(USR_REPOSITORY.findByUsername(PREDEFINED_USER.username()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));
            when(USR_REPOSITORY.save(any(User.class)))
                    .thenReturn(MAPPER.requestDtoToEntity(userRequestDTO));
            when(INJECTED_MAPPER.entityToResponseDTO(any(User.class)))
                    .thenReturn(toResponseDTO(userRequestDTO));

            assertDoesNotThrow(() -> CRUD_SERVICE.update(PREDEFINED_USER.uuid(), userRequestDTO));
            assertThat(CRUD_SERVICE.update(PREDEFINED_USER.uuid(), userRequestDTO))
                    .isEqualTo(toResponseDTO(userRequestDTO));

            verify(USR_REPOSITORY, times(2))
                    .findByUuid(PREDEFINED_USER.uuid());
            verify(USR_REPOSITORY, times(2))
                    .save(any(User.class));
            verify(INJECTED_MAPPER, times(2))
                    .entityToResponseDTO(any(User.class));
        }

        @ParameterizedTest(name = "{index}: username={0}")
        @DisplayName("UserService#update should throw UserNotFoundException when user with given uuid does not exist")
        @ValueSource(strings = NONEXISTENT_UUID)
        void update_shouldThrow_whenUserDoesNotExist(String uuidStr) {
            val uuid = UUID.fromString(uuidStr);

            assertThatExceptionOfType(UserNotFoundException.class)
                    .isThrownBy(() -> CRUD_SERVICE.update(uuid, UserRequestDTO.builder().build()));
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("UserService#update should throw EmailFormatException when email has invalid formatting")
        @MethodSource("util.UserFlowTestDataFactory#invalidEmails")
        void update_shouldThrow_whenEmailHasInvalidFormatting(String email) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username("valid_username")
                    .build();

            when(USR_REPOSITORY.findByUuid(PREDEFINED_USER.uuid()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));

            assertThatExceptionOfType(EmailFormatException.class)
                    .isThrownBy(() -> CRUD_SERVICE.update(PREDEFINED_USER.uuid(), userRequestDTO));

            verify(USR_REPOSITORY)
                    .findByUuid(PREDEFINED_USER.uuid());
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("UserService#update should throw UsernameFormatException when email has invalid formatting")
        @MethodSource("util.UserFlowTestDataFactory#invalidEmails")
        void update_shouldThrow_whenUsernameHasInvalidFormatting(String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email("valid@email.com")
                    .username(username)
                    .build();

            when(USR_REPOSITORY.findByUuid(PREDEFINED_USER.uuid()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));

            assertThatExceptionOfType(UsernameFormatException.class)
                    .isThrownBy(() -> CRUD_SERVICE.update(PREDEFINED_USER.uuid(), userRequestDTO));

            verify(USR_REPOSITORY)
                    .findByUuid(PREDEFINED_USER.uuid());
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("UserService#update should throw EmailTakenException when email is taken")
        @MethodSource("util.UserFlowTestDataFactory#validUserDataWithEmailConflict")
        void update_shouldThrow_whenEmailTaken(String email, String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username(username)
                    .build();

            val conflictingUser = new User();
            conflictingUser.setUuid(UUID.randomUUID());
            conflictingUser.setEmail("conflicting@user.com");
            conflictingUser.setUsername("conflicting_user");

            when(USR_REPOSITORY.findByUuid(conflictingUser.getUuid()))
                    .thenReturn(Optional.of(conflictingUser));
            when(USR_REPOSITORY.findByEmail(PREDEFINED_USER.email()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));
            when(USR_REPOSITORY.findByUsername(PREDEFINED_USER.username()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));

            assertThatExceptionOfType(EmailTakenException.class)
                    .isThrownBy(() -> CRUD_SERVICE.update(conflictingUser.getUuid(), userRequestDTO));

            verify(USR_REPOSITORY)
                    .findByUuid(conflictingUser.getUuid());
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("UserService#update should throw UsernameTakenException when username is taken")
        @MethodSource("util.UserFlowTestDataFactory#validUserDataWithUsernameConflict")
        void update_shouldThrow_whenUsernameTaken(String email, String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username(username)
                    .build();

            val conflictingUser = new User();
            conflictingUser.setUuid(UUID.randomUUID());
            conflictingUser.setEmail("conflicting@user.com");
            conflictingUser.setUsername("conflicting_user");

            when(USR_REPOSITORY.findByUuid(conflictingUser.getUuid()))
                    .thenReturn(Optional.of(conflictingUser));
            when(USR_REPOSITORY.findByEmail(PREDEFINED_USER.email()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));
            when(USR_REPOSITORY.findByUsername(PREDEFINED_USER.username()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));

            assertThatExceptionOfType(UsernameTakenException.class)
                    .isThrownBy(() -> CRUD_SERVICE.update(conflictingUser.getUuid(), userRequestDTO));

            verify(USR_REPOSITORY)
                    .findByUuid(conflictingUser.getUuid());
        }

    }

    @Nested
    class Delete {

        @ParameterizedTest(name = "{index}: uuid={0}")
        @DisplayName("UserService#delete should not throw when user with given uuid exists")
        @ValueSource(strings = PREDEFINED_UUID)
        void delete_shouldNotThrow_whenUserExists(String uuidStr) {
            val uuid = UUID.fromString(uuidStr);

            when(USR_REPOSITORY.findByUuid(uuid))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));

            assertDoesNotThrow(() -> CRUD_SERVICE.delete(uuid));

            verify(USR_REPOSITORY)
                    .findByUuid(uuid);
        }

        @ParameterizedTest(name = "{index}: uuid={0}")
        @DisplayName("UserService#delete should throw UserNotFoundException when user with given uuid does not exist")
        @ValueSource(strings = NONEXISTENT_UUID)
        void delete_shouldThrow_whenUserDoesNotExist(String uuidStr) {
            val uuid = UUID.fromString(uuidStr);

            assertThatExceptionOfType(UserNotFoundException.class)
                    .isThrownBy(() -> CRUD_SERVICE.delete(uuid));
        }

    }
}

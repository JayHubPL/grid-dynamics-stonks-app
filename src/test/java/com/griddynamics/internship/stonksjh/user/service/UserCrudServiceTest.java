package com.griddynamics.internship.stonksjh.user.service;

import com.griddynamics.internship.stonksjh.user.dto.CrudRequestDTO;
import com.griddynamics.internship.stonksjh.user.dto.UserDTO;
import com.griddynamics.internship.stonksjh.user.exception.exceptions.ConflictException;
import com.griddynamics.internship.stonksjh.user.exception.exceptions.DataFormatException;
import com.griddynamics.internship.stonksjh.user.exception.exceptions.UserNotFoundException;
import com.griddynamics.internship.stonksjh.user.mapper.UserMapper;
import com.griddynamics.internship.stonksjh.user.model.User;
import com.griddynamics.internship.stonksjh.user.repository.UserRepository;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({MockitoExtension.class, SpringExtension.class})
class UserCrudServiceTest {

    @MockBean
    private UserRepository USR_REPOSITORY;
    @MockBean
    private UserMapper INJECTED_MAPPER;
    private UserCrudService CRUD_SERVICE;
    private final UserMapper MAPPER = Mappers.getMapper(UserMapper.class);
    private final String PREDEFINED_UUID = "b16fbf04-09d5-4758-a6d6-a9200e5af2c9";
    private final String NONEXISTENT_UUID = "ab8f2253-d6c1-4adc-bd60-18e16916fb23";
    private UserDTO PREDEFINED_USER;
    private CrudRequestDTO crudRequestDTO;

    @BeforeAll
    void initCrudService() {
        CRUD_SERVICE = new UserCrudService(USR_REPOSITORY, INJECTED_MAPPER);
    }

    @BeforeAll
    void initPredefinedUser() {
        PREDEFINED_USER = new UserDTO();
        PREDEFINED_USER.setUuid(UUID.fromString(PREDEFINED_UUID));
        PREDEFINED_USER.setEmail("user@example.com");
        PREDEFINED_USER.setUsername("user");
        PREDEFINED_USER.setBalance(BigDecimal.ZERO);
    }

    @BeforeEach
    void initCrudRequestDTO() {
        crudRequestDTO = new CrudRequestDTO();
    }

    @Nested
    class Create {

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("UserCrudService#create should not throw an exception when user data is correct")
        @MethodSource("util.TestDataFactory#validUserData")
        void create_shouldNotThrow_whenUserDataOk(String email, String username) {
            crudRequestDTO.setEmail(email);
            crudRequestDTO.setUsername(username);

            when(INJECTED_MAPPER.requestDtoToEntity(crudRequestDTO))
                    .thenReturn(MAPPER.requestDtoToEntity(crudRequestDTO));

            assertDoesNotThrow(() -> CRUD_SERVICE.create(crudRequestDTO));

            verify(INJECTED_MAPPER)
                    .requestDtoToEntity(crudRequestDTO);
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("UserCrudService#create should throw UserEmailFormatException when email has illegal formatting")
        @MethodSource("util.TestDataFactory#invalidUserData")
        void create_shouldThrow_whenCredentialsHaveInvalidFormatting(String email, String username) {
            crudRequestDTO.setEmail(email);
            crudRequestDTO.setUsername(username);

            assertThatExceptionOfType(DataFormatException.class)
                    .isThrownBy(() -> CRUD_SERVICE.create(crudRequestDTO));
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("UserCrudService#create should throw ConflictException when username or email is taken")
        @MethodSource("util.TestDataFactory#validUserDataWithConflicts")
        void create_shouldThrow_whenCredentialsAreTaken(String email, String username) {
            crudRequestDTO.setEmail(email);
            crudRequestDTO.setUsername(username);

            doReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)))
                    .when(USR_REPOSITORY)
                    .findByEmail(PREDEFINED_USER.getEmail());
            doReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)))
                    .when(USR_REPOSITORY)
                    .findByUsername(PREDEFINED_USER.getUsername());

            assertThatExceptionOfType(ConflictException.class)
                    .isThrownBy(() -> CRUD_SERVICE.create(crudRequestDTO));
        }

    }

    @Nested
    class Read {

        @ParameterizedTest(name = "{index}: uuid={0}")
        @DisplayName("UserCrudService#readOne should not throw an exception when user with given uuid exists")
        @ValueSource(strings = PREDEFINED_UUID)
        void readOne_shouldNotThrow_whenUserExists(String uuidStr) {
            val uuid = UUID.fromString(uuidStr);

            when(USR_REPOSITORY.findByUuid(uuid))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));
            when(INJECTED_MAPPER.entityToDto(any(User.class)))
                    .thenReturn(PREDEFINED_USER);

            assertDoesNotThrow(() -> CRUD_SERVICE.readOne(uuid));
            assertThat(CRUD_SERVICE.readOne(uuid))
                    .isEqualTo(PREDEFINED_USER);

            verify(USR_REPOSITORY, times(2))
                    .findByUuid(uuid);
            verify(INJECTED_MAPPER, times(2))
                    .entityToDto(any(User.class));
        }

        @ParameterizedTest(name = "{index}: uuid={0}")
        @DisplayName("UserCrudService#readOne should throw UserNotFoundException when user with given uuid does not exist")
        @ValueSource(strings = NONEXISTENT_UUID)
        void readOne_shouldThrow_whenUserDoesNotExist(String uuidStr) {
            val uuid = UUID.fromString(uuidStr);

            when(USR_REPOSITORY.findByUuid(uuid))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(UserNotFoundException.class)
                    .isThrownBy(() -> CRUD_SERVICE.readOne(uuid));

            verify(USR_REPOSITORY)
                    .findByUuid(uuid);
        }

        @Test
        @DisplayName("UserCrudService#readAll should return an empty list when no users exist")
        void readAll_shouldReturnEmptyList_whenNoUsersExist() {
            when(USR_REPOSITORY.findAll())
                    .thenReturn(List.of());

            assertThat(CRUD_SERVICE.readAll())
                    .isEmpty();

            verify(USR_REPOSITORY)
                    .findAll();
        }

        @Test
        @DisplayName("UserCrudService#readAll should return a non-empty list when any users exist")
        void readAll_shouldReturnNonEmptyList_whenUsersExist() {
            when(USR_REPOSITORY.findAll())
                    .thenReturn(List.of(MAPPER.dtoToEntity(PREDEFINED_USER)));
            when(INJECTED_MAPPER.entityToDto(any(User.class)))
                    .thenReturn(PREDEFINED_USER);

            assertDoesNotThrow(() -> CRUD_SERVICE.readAll());
            val list = CRUD_SERVICE.readAll();
            assertThat(list)
                    .isNotEmpty();
            assertThat(list)
                    .isEqualTo(List.of(PREDEFINED_USER));

            verify(USR_REPOSITORY, times(2))
                    .findAll();
            verify(INJECTED_MAPPER, times(2))
                    .entityToDto(any(User.class));
        }

    }

    @Nested
    class Update {

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("UserCrudService#update should return updated dto after updating existing user")
        @MethodSource("util.TestDataFactory#validUserData")
        void update_shouldReturnCorrectDTO_whenUserDataOk(String email, String username) {
            crudRequestDTO.setEmail(email);
            crudRequestDTO.setUsername(username);

            when(USR_REPOSITORY.findByUuid(PREDEFINED_USER.getUuid()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));
            when(USR_REPOSITORY.findByEmail(PREDEFINED_USER.getEmail()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));
            when(USR_REPOSITORY.findByUsername(PREDEFINED_USER.getUsername()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));
            when(USR_REPOSITORY.save(any(User.class)))
                    .thenReturn(MAPPER.requestDtoToEntity(crudRequestDTO));
            when(INJECTED_MAPPER.entityToDto(any(User.class)))
                    .thenReturn(toUserDTO(crudRequestDTO));

            assertDoesNotThrow(() -> CRUD_SERVICE.update(PREDEFINED_USER.getUuid(), crudRequestDTO));
            assertThat(CRUD_SERVICE.update(PREDEFINED_USER.getUuid(), crudRequestDTO))
                    .isEqualTo(toUserDTO(crudRequestDTO));

            verify(USR_REPOSITORY, times(2))
                    .findByUuid(PREDEFINED_USER.getUuid());
            verify(USR_REPOSITORY, times(2))
                    .save(any(User.class));
            verify(INJECTED_MAPPER, times(2))
                    .entityToDto(any(User.class));
        }

        @ParameterizedTest(name = "{index}: username={0}")
        @DisplayName("UserCrudService#update should throw UserNotFoundException when user with given uuid does not exist")
        @ValueSource(strings = NONEXISTENT_UUID)
        void update_shouldThrow_whenUserDoesNotExist(String uuidStr) {
            val uuid = UUID.fromString(uuidStr);

            assertThatExceptionOfType(UserNotFoundException.class)
                    .isThrownBy(() -> CRUD_SERVICE.update(uuid, new CrudRequestDTO()));
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("UserCrudService#update should throw DataFormatException when email or username has invalid formatting")
        @MethodSource("util.TestDataFactory#invalidUserData")
        void update_shouldThrow_whenCredentialsHaveInvalidFormatting(String email, String username) {
            crudRequestDTO.setEmail(email);
            crudRequestDTO.setUsername(username);

            when(USR_REPOSITORY.findByUuid(PREDEFINED_USER.getUuid()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));

            assertThatExceptionOfType(DataFormatException.class)
                    .isThrownBy(() -> CRUD_SERVICE.update(PREDEFINED_USER.getUuid(), crudRequestDTO));

            verify(USR_REPOSITORY)
                    .findByUuid(PREDEFINED_USER.getUuid());
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("UserCrudService#update should throw ConflictException when email or username is taken")
        @MethodSource("util.TestDataFactory#validUserDataWithConflicts")
        void update_shouldThrow_whenEmailOrUsernameTaken(String email, String username) {
            crudRequestDTO.setEmail(email);
            crudRequestDTO.setUsername(username);

            val conflictingUser = new User();
            conflictingUser.setUuid(UUID.randomUUID());
            conflictingUser.setEmail("conflicting@user.com");
            conflictingUser.setUsername("conflicting_user");

            when(USR_REPOSITORY.findByUuid(conflictingUser.getUuid()))
                    .thenReturn(Optional.of(conflictingUser));
            when(USR_REPOSITORY.findByEmail(PREDEFINED_USER.getEmail()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));
            when(USR_REPOSITORY.findByUsername(PREDEFINED_USER.getUsername()))
                    .thenReturn(Optional.of(MAPPER.dtoToEntity(PREDEFINED_USER)));

            assertThatExceptionOfType(ConflictException.class)
                    .isThrownBy(() -> CRUD_SERVICE.update(conflictingUser.getUuid(), crudRequestDTO));

            verify(USR_REPOSITORY)
                    .findByUuid(conflictingUser.getUuid());
        }

    }

    @Nested
    class Delete {

        @ParameterizedTest(name = "{index}: uuid={0}")
        @DisplayName("UserCrudService#delete should not throw when user with given uuid exists")
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
        @DisplayName("UserCrudService#delete should throw UserNotFoundException when user with given uuid does not exist")
        @ValueSource(strings = NONEXISTENT_UUID)
        void delete_shouldThrow_whenUserDoesNotExist(String uuidStr) {
            val uuid = UUID.fromString(uuidStr);

            assertThatExceptionOfType(UserNotFoundException.class)
                    .isThrownBy(() -> CRUD_SERVICE.delete(uuid));
        }

    }

    private UserDTO toUserDTO(CrudRequestDTO crudRequestDTO) {
        val userDTO = new UserDTO();

        userDTO.setUuid(UUID.fromString(PREDEFINED_UUID));
        userDTO.setEmail(crudRequestDTO.getEmail());
        userDTO.setUsername(crudRequestDTO.getUsername());
        userDTO.setBalance(BigDecimal.ZERO);

        return userDTO;
    }
}

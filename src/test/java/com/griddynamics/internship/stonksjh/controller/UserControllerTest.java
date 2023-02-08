package com.griddynamics.internship.stonksjh.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.griddynamics.internship.stonksjh.dto.user.UserRequestDTO;
import com.griddynamics.internship.stonksjh.dto.user.UserResponseDTO;
import com.griddynamics.internship.stonksjh.exception.user.EmailFormatException;
import com.griddynamics.internship.stonksjh.exception.user.EmailTakenException;
import com.griddynamics.internship.stonksjh.exception.user.UserNotFoundException;
import com.griddynamics.internship.stonksjh.exception.user.UsernameFormatException;
import com.griddynamics.internship.stonksjh.exception.user.UsernameTakenException;
import com.griddynamics.internship.stonksjh.service.UserService;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest(UserController.class)
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private final String PREDEFINED_UUID = "b16fbf04-09d5-4758-a6d6-a9200e5af2c9";
    private final String NONEXISTENT_UUID = "ab8f2253-d6c1-4adc-bd60-18e16916fb23";
    @MockBean
    UserService USR_CRUD_SERVICE;
    @Autowired
    private MockMvc MVC;
    private UserResponseDTO PREDEFINED_USR;

    @BeforeAll
    void initPredefinedUser() {
        PREDEFINED_USR = UserResponseDTO.builder()
                .uuid(UUID.fromString(PREDEFINED_UUID))
                .email("user@example.com")
                .username("user")
                .balance(BigDecimal.ZERO)
                .build();
    }

    private String jsonString(UserRequestDTO userRequestDTO) {
        try {
            return new ObjectMapper().writeValueAsString(userRequestDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting UserDTO to json", e);
        }
    }

    @Nested
    class Create {
        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("/create endpoint should return HTTP 201 response with correct location header when user data is correct")
        @MethodSource("util.UserFlowTestDataFactory#validUserData")
        @SneakyThrows
        void create_shouldReturnCreatedResponse_whenUserOk(String email, String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username(username)
                    .build();

            when(USR_CRUD_SERVICE.create(userRequestDTO))
                    .thenReturn(UUID.fromString(PREDEFINED_UUID));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(UserController.class.getMethod("create", UserRequestDTO.class), userRequestDTO).toUri())
                            .content(jsonString(userRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$").value(PREDEFINED_USR.uuid().toString()));

            verify(USR_CRUD_SERVICE)
                    .create(userRequestDTO);
        }

        @ParameterizedTest(name = "{index}: email={0}")
        @DisplayName("/create endpoint should return HTTP 400 response when email has illegal formatting")
        @MethodSource("util.UserFlowTestDataFactory#invalidEmails")
        @SneakyThrows
        void create_shouldReturnBadRequestResponse_whenEmailHasIllegalFormatting(String email) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username("valid_username")
                    .build();

            when(USR_CRUD_SERVICE.create(userRequestDTO))
                    .thenThrow(new EmailFormatException("Some placeholder message"));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(UserController.class.getMethod("create", UserRequestDTO.class), userRequestDTO).toUri())
                            .content(jsonString(userRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .create(userRequestDTO);
        }

        @ParameterizedTest(name = "{index}: username={0}")
        @DisplayName("/create endpoint should return HTTP 400 response when username has illegal formatting")
        @MethodSource("util.UserFlowTestDataFactory#invalidUsernames")
        @SneakyThrows
        void create_shouldReturnBadRequestResponse_whenUsernameHasIllegalFormatting(String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email("valid@email.com")
                    .username(username)
                    .build();

            when(USR_CRUD_SERVICE.create(userRequestDTO))
                    .thenThrow(new EmailFormatException("Some placeholder message"));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(UserController.class.getMethod("create", UserRequestDTO.class), userRequestDTO).toUri())
                            .content(jsonString(userRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .create(userRequestDTO);
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("/create endpoint should return HTTP 409 response when email is already taken")
        @MethodSource("util.UserFlowTestDataFactory#validUserDataWithEmailConflict")
        @SneakyThrows
        void create_shouldReturnConflictResponse_whenEmailIsTaken(String email, String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username(username)
                    .build();

            when(USR_CRUD_SERVICE.create(userRequestDTO))
                    .thenThrow(new EmailTakenException(email));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(UserController.class.getMethod("create", UserRequestDTO.class), userRequestDTO).toUri())
                            .content(jsonString(userRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .create(userRequestDTO);
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("/create endpoint should return HTTP 409 response when username is already taken")
        @MethodSource("util.UserFlowTestDataFactory#validUserDataWithUsernameConflict")
        @SneakyThrows
        void create_shouldReturnConflictResponse_whenUsernameIsTaken(String email, String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username(username)
                    .build();

            when(USR_CRUD_SERVICE.create(userRequestDTO))
                    .thenThrow(new UsernameTakenException(username));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(UserController.class.getMethod("create", UserRequestDTO.class), userRequestDTO).toUri())
                            .content(jsonString(userRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .create(userRequestDTO);
        }

    }

    @Nested
    class Read {
        @ParameterizedTest(name = "{index}: username={0}")
        @DisplayName("/get/{username} endpoint should return HTTP 200 response with correct body when user exists")
        @ValueSource(strings = PREDEFINED_UUID)
        @SneakyThrows
        void readOne_shouldReturnOkResponse_whenUserExists(String uuidStr) {
            val uuid = UUID.fromString(uuidStr);

            when(USR_CRUD_SERVICE.read(uuid))
                    .thenReturn(PREDEFINED_USR);

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(UserController.class.getMethod("read", UUID.class), uuid).toUri())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(PREDEFINED_USR.email()))
                    .andExpect(jsonPath("$.username").value(PREDEFINED_USR.username()));

            verify(USR_CRUD_SERVICE)
                    .read(uuid);
        }

        @ParameterizedTest(name = "{index}: username={0}")
        @DisplayName("/get/{username} endpoint should return HTTP 404 response when user does not exist")
        @ValueSource(strings = PREDEFINED_UUID)
        @SneakyThrows
        void readOne_shouldReturnNotFoundResponse_whenUserDoesNotExists(String uuidStr) {
            val uuid = UUID.fromString(uuidStr);

            when(USR_CRUD_SERVICE.read(uuid))
                    .thenThrow(new UserNotFoundException(uuid));

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(UserController.class.getMethod("read", UUID.class), uuid).toUri())
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .read(uuid);
        }

        @Test
        @DisplayName("/get endpoint should return HTTP 200 response with non empty list in body when there are some users")
        @SneakyThrows
        void readAll_shouldReturnOkResponse_andNonEmptyList_whenUsersExist() {
            when(USR_CRUD_SERVICE.read())
                    .thenReturn(List.of(PREDEFINED_USR));

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(UserController.class.getMethod("read")).toUri())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].email").value(PREDEFINED_USR.email()))
                    .andExpect(jsonPath("$[0].username").value(PREDEFINED_USR.username()));

            verify(USR_CRUD_SERVICE)
                    .read();
        }

        @Test
        @DisplayName("/get endpoint should return HTTP 200 response with empty list in body when there are no users")
        @SneakyThrows
        void readAll_shouldReturnOkResponse_andEmptyList_whenNoUsersExist() {
            when(USR_CRUD_SERVICE.read())
                    .thenReturn(List.of());

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(UserController.class.getMethod("read")).toUri())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());

            verify(USR_CRUD_SERVICE)
                    .read();
        }
    }

    @Nested
    class Update {
        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("/update endpoint should return HTTP 200 response when user exists and the data is correct")
        @MethodSource("util.UserFlowTestDataFactory#validUserData")
        @SneakyThrows
        void update_shouldReturnOkResponse_whenUserDataOk(String email, String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username(username)
                    .build();

            val updated = UserResponseDTO.builder()
                    .uuid(PREDEFINED_USR.uuid())
                    .email(email)
                    .username(username)
                    .build();

            when(USR_CRUD_SERVICE.update(PREDEFINED_USR.uuid(), userRequestDTO))
                    .thenReturn(updated);

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(UserController.class.getMethod("update", UUID.class, UserRequestDTO.class), PREDEFINED_USR.uuid(), userRequestDTO).toUri())
                            .content(jsonString(userRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(userRequestDTO.email()))
                    .andExpect(jsonPath("$.username").value(userRequestDTO.username()));

            verify(USR_CRUD_SERVICE)
                    .update(PREDEFINED_USR.uuid(), userRequestDTO);
        }

        @ParameterizedTest(name = "{index}: email={0}")
        @DisplayName("/update endpoint should return HTTP 400 response when email has invalid formatting")
        @MethodSource("util.UserFlowTestDataFactory#invalidEmails")
        @SneakyThrows
        void update_shouldReturnBadRequestResponse_whenEmailHasInvalidFormatting(String email) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username("valid_username")
                    .build();

            when(USR_CRUD_SERVICE.update(PREDEFINED_USR.uuid(), userRequestDTO))
                    .thenThrow(new EmailFormatException(email));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(UserController.class.getMethod("update", UUID.class, UserRequestDTO.class), PREDEFINED_USR.uuid(), userRequestDTO).toUri())
                            .content(jsonString(userRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .update(PREDEFINED_USR.uuid(), userRequestDTO);
        }

        @ParameterizedTest(name = "{index}: username={0}")
        @DisplayName("/update endpoint should return HTTP 400 response when username has invalid formatting")
        @MethodSource("util.UserFlowTestDataFactory#invalidUsernames")
        @SneakyThrows
        void update_shouldReturnBadRequestResponse_whenUsernameHasInvalidFormatting(String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email("valid@email.com")
                    .username(username)
                    .build();

            when(USR_CRUD_SERVICE.update(PREDEFINED_USR.uuid(), userRequestDTO))
                    .thenThrow(new UsernameFormatException(username));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(UserController.class.getMethod("update", UUID.class, UserRequestDTO.class), PREDEFINED_USR.uuid(), userRequestDTO).toUri())
                            .content(jsonString(userRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .update(PREDEFINED_USR.uuid(), userRequestDTO);
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("/update endpoint should return HTTP 404 response when user does not exist")
        @MethodSource("util.UserFlowTestDataFactory#validUserData")
        @SneakyThrows
        void update_shouldReturnNotFoundResponse_whenUserDoesNotExist(String email, String username) {
            val nonexistent = UUID.fromString(NONEXISTENT_UUID);
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username(username)
                    .build();

            when(USR_CRUD_SERVICE.update(nonexistent, userRequestDTO))
                    .thenThrow(new UserNotFoundException(nonexistent));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(UserController.class.getMethod("update", UUID.class, UserRequestDTO.class), nonexistent, userRequestDTO).toUri())
                            .content(jsonString(userRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .update(nonexistent, userRequestDTO);
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("/update endpoint should return HTTP 409 response when email is already taken")
        @MethodSource("util.UserFlowTestDataFactory#validUserDataWithEmailConflict")
        @SneakyThrows
        void update_shouldReturnConflictResponse_whenEmailIsTaken(String email, String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username(username)
                    .build();

            when(USR_CRUD_SERVICE.update(PREDEFINED_USR.uuid(), userRequestDTO))
                    .thenThrow(new EmailTakenException(email));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(UserController.class.getMethod("update", UUID.class, UserRequestDTO.class), PREDEFINED_USR.uuid(), userRequestDTO).toUri())
                            .content(jsonString(userRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .update(PREDEFINED_USR.uuid(), userRequestDTO);
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("/update endpoint should return HTTP 409 response when username is already taken")
        @MethodSource("util.UserFlowTestDataFactory#validUserDataWithEmailConflict")
        @SneakyThrows
        void update_shouldReturnConflictResponse_whenUsernameIsTaken(String email, String username) {
            val userRequestDTO = UserRequestDTO.builder()
                    .email(email)
                    .username(username)
                    .build();

            when(USR_CRUD_SERVICE.update(PREDEFINED_USR.uuid(), userRequestDTO))
                    .thenThrow(new UsernameTakenException(username));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(UserController.class.getMethod("update", UUID.class, UserRequestDTO.class), PREDEFINED_USR.uuid(), userRequestDTO).toUri())
                            .content(jsonString(userRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .update(PREDEFINED_USR.uuid(), userRequestDTO);
        }

    }

    @Nested
    class Delete {
        @ParameterizedTest(name = "{index}: username={0}")
        @DisplayName("/delete endpoint should return HTTP 200 response when user exists")
        @ValueSource(strings = PREDEFINED_UUID)
        @SneakyThrows
        void delete_shouldReturnOkResponse_whenUserExists(String uuidStr) {
            val uuid = UUID.fromString(uuidStr);

            doNothing()
                    .when(USR_CRUD_SERVICE)
                    .delete(uuid);

            MVC.perform(MockMvcRequestBuilders
                            .delete(linkTo(UserController.class.getMethod("delete", UUID.class), uuid).toUri())
                    )
                    .andExpect(status().isOk());

            verify(USR_CRUD_SERVICE)
                    .delete(uuid);
        }

        @ParameterizedTest(name = "{index}: username={0}")
        @DisplayName("/delete endpoint should return HTTP 404 response when user does not exist")
        @ValueSource(strings = NONEXISTENT_UUID)
        @SneakyThrows
        void delete_shouldReturnNotFoundResponse_whenUserDoesNotExist(String uuidStr) {
            val uuid = UUID.fromString(uuidStr);

            doThrow(UserNotFoundException.class)
                    .when(USR_CRUD_SERVICE)
                    .delete(uuid);

            MVC.perform(MockMvcRequestBuilders
                            .delete(linkTo(UserController.class.getMethod("delete", UUID.class), uuid).toUri())
                    )
                    .andExpect(status().isNotFound())
                    //.andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .delete(uuid);
        }
    }

}

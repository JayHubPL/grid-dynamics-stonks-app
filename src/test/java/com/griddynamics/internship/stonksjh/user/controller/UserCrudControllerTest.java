package com.griddynamics.internship.stonksjh.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.griddynamics.internship.stonksjh.user.dto.CrudRequestDTO;
import com.griddynamics.internship.stonksjh.user.dto.UserDTO;
import com.griddynamics.internship.stonksjh.user.exception.exceptions.ConflictException;
import com.griddynamics.internship.stonksjh.user.exception.exceptions.DataFormatException;
import com.griddynamics.internship.stonksjh.user.exception.exceptions.UserNotFoundException;
import com.griddynamics.internship.stonksjh.user.service.UserCrudService;
import lombok.SneakyThrows;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest(UserCrudController.class)
@ExtendWith(MockitoExtension.class)
class UserCrudControllerTest {

    @Autowired
    private MockMvc MVC;
    @MockBean
    UserCrudService USR_CRUD_SERVICE;
    private final String PREDEFINED_UUID = "b16fbf04-09d5-4758-a6d6-a9200e5af2c9";
    private final String NONEXISTENT_UUID = "ab8f2253-d6c1-4adc-bd60-18e16916fb23";
    private UserDTO PREDEFINED_USR;
    private CrudRequestDTO crudRequestDTO;

    @BeforeAll
    void initPredefinedUser() {
        PREDEFINED_USR = new UserDTO();
        PREDEFINED_USR.setUuid(UUID.fromString(PREDEFINED_UUID));
        PREDEFINED_USR.setEmail("user@example.com");
        PREDEFINED_USR.setUsername("user");
        PREDEFINED_USR.setBalance(BigDecimal.ZERO);
    }

    @BeforeEach
    void initCrudRequestDTO() {
        crudRequestDTO = new CrudRequestDTO();
    }

    @Nested
    class Create {
        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("/create endpoint should return HTTP 201 response with correct location header when user data is correct")
        @MethodSource("util.TestDataFactory#validUserData")
        @SneakyThrows
        void create_shouldReturnCreatedResponse_whenUserOk(String email, String username) {
            crudRequestDTO.setEmail(email);
            crudRequestDTO.setUsername(username);

            when(USR_CRUD_SERVICE.create(crudRequestDTO))
                    .thenReturn(UUID.fromString(PREDEFINED_UUID));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(UserCrudController.class.getMethod("create", CrudRequestDTO.class), crudRequestDTO).toUri())
                            .content(jsonString(crudRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$").value(PREDEFINED_USR.getUuid().toString()));

            verify(USR_CRUD_SERVICE)
                    .create(crudRequestDTO);
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("/create endpoint should return HTTP 400 response when email or username has illegal formatting")
        @MethodSource("util.TestDataFactory#invalidUserData")
        @SneakyThrows
        void create_shouldReturnBadRequestResponse_whenDataHasIllegalFormatting(String email, String username) {
            crudRequestDTO.setEmail(email);
            crudRequestDTO.setUsername(username);

            when(USR_CRUD_SERVICE.create(crudRequestDTO))
                    .thenThrow(new DataFormatException("Some placeholder message"));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(UserCrudController.class.getMethod("create", CrudRequestDTO.class), crudRequestDTO).toUri())
                            .content(jsonString(crudRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .create(crudRequestDTO);
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("/create endpoint should return HTTP 409 response when email or username is already taken")
        @MethodSource("util.TestDataFactory#validUserDataWithConflicts")
        @SneakyThrows
        void create_shouldReturnConflictResponse_whenUsernameOrEmailIsTaken(String email, String username) {
            crudRequestDTO.setEmail(email);
            crudRequestDTO.setUsername(username);

            when(USR_CRUD_SERVICE.create(crudRequestDTO))
                    .thenThrow(new ConflictException("Some placeholder message"));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(UserCrudController.class.getMethod("create", CrudRequestDTO.class), crudRequestDTO).toUri())
                            .content(jsonString(crudRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .create(crudRequestDTO);
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

            when(USR_CRUD_SERVICE.readOne(uuid))
                    .thenReturn(PREDEFINED_USR);

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(UserCrudController.class.getMethod("readOne", UUID.class), uuid).toUri())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(PREDEFINED_USR.getEmail()))
                    .andExpect(jsonPath("$.username").value(PREDEFINED_USR.getUsername()));

            verify(USR_CRUD_SERVICE)
                    .readOne(uuid);
        }

        @ParameterizedTest(name = "{index}: username={0}")
        @DisplayName("/get/{username} endpoint should return HTTP 404 response when user does not exist")
        @ValueSource(strings = PREDEFINED_UUID)
        @SneakyThrows
        void readOne_shouldReturnNotFoundResponse_whenUserDoesNotExists(String uuidStr) {
            val uuid = UUID.fromString(uuidStr);

            when(USR_CRUD_SERVICE.readOne(uuid))
                    .thenThrow(new UserNotFoundException(uuid));

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(UserCrudController.class.getMethod("readOne", UUID.class), uuid).toUri())
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .readOne(uuid);
        }

        @Test
        @DisplayName("/get endpoint should return HTTP 200 response with non empty list in body when there are some users")
        @SneakyThrows
        void readAll_shouldReturnOkResponse_andNonEmptyList_whenUsersExist() {
            when(USR_CRUD_SERVICE.readAll())
                    .thenReturn(List.of(PREDEFINED_USR));

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(UserCrudController.class.getMethod("readAll")).toUri())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].email").value(PREDEFINED_USR.getEmail()))
                    .andExpect(jsonPath("$[0].username").value(PREDEFINED_USR.getUsername()));

            verify(USR_CRUD_SERVICE)
                    .readAll();
        }

        @Test
        @DisplayName("/get endpoint should return HTTP 200 response with empty list in body when there are no users")
        @SneakyThrows
        void readAll_shouldReturnOkResponse_andEmptyList_whenNoUsersExist() {
            when(USR_CRUD_SERVICE.readAll())
                    .thenReturn(List.of());

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(UserCrudController.class.getMethod("readAll")).toUri())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());

            verify(USR_CRUD_SERVICE)
                    .readAll();
        }
    }

    @Nested
    class Update {
        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("/update endpoint should return HTTP 200 response when user exists and the data is correct")
        @MethodSource("util.TestDataFactory#validUserData")
        @SneakyThrows
        void update_shouldReturnOkResponse_whenUserDataOk(String email, String username) {
            crudRequestDTO.setEmail(email);
            crudRequestDTO.setUsername(username);

            val updated = new UserDTO();
            updated.setUuid(PREDEFINED_USR.getUuid());
            updated.setEmail(email);
            updated.setUsername(username);

            when(USR_CRUD_SERVICE.update(PREDEFINED_USR.getUuid(), crudRequestDTO))
                    .thenReturn(updated);

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(UserCrudController.class.getMethod("update", UUID.class, CrudRequestDTO.class), PREDEFINED_USR.getUuid(), crudRequestDTO).toUri())
                            .content(jsonString(crudRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(crudRequestDTO.getEmail()))
                    .andExpect(jsonPath("$.username").value(crudRequestDTO.getUsername()));

            verify(USR_CRUD_SERVICE)
                    .update(PREDEFINED_USR.getUuid(), crudRequestDTO);
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("/update endpoint should return HTTP 400 response when email or username has invalid formatting")
        @MethodSource("util.TestDataFactory#invalidUserData")
        @SneakyThrows
        void update_shouldReturnBadRequestResponse_whenEmailOrUsernameHasInvalidFormatting(String email, String username) {
            crudRequestDTO.setEmail(email);
            crudRequestDTO.setUsername(username);

            when(USR_CRUD_SERVICE.update(PREDEFINED_USR.getUuid(), crudRequestDTO))
                    .thenThrow(new DataFormatException("Some placeholder message"));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(UserCrudController.class.getMethod("update", UUID.class, CrudRequestDTO.class), PREDEFINED_USR.getUuid(), crudRequestDTO).toUri())
                            .content(jsonString(crudRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .update(PREDEFINED_USR.getUuid(), crudRequestDTO);
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("/update endpoint should return HTTP 404 response when user does not exist")
        @MethodSource("util.TestDataFactory#validUserData")
        @SneakyThrows
        void update_shouldReturnNotFoundResponse_whenUserDoesNotExist(String email, String username) {
            val nonexistent = UUID.fromString(NONEXISTENT_UUID);
            crudRequestDTO.setEmail(email);
            crudRequestDTO.setUsername(username);

            when(USR_CRUD_SERVICE.update(nonexistent, crudRequestDTO))
                    .thenThrow(new UserNotFoundException(nonexistent));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(UserCrudController.class.getMethod("update", UUID.class, CrudRequestDTO.class), nonexistent, crudRequestDTO).toUri())
                            .content(jsonString(crudRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .update(nonexistent, crudRequestDTO);
        }

        @ParameterizedTest(name = "{index}: user=[{0},{1}]")
        @DisplayName("/update endpoint should return HTTP 409 response when email or username is already taken")
        @MethodSource("util.TestDataFactory#validUserDataWithConflicts")
        @SneakyThrows
        void update_shouldReturnConflictResponse_whenEmailOrUsernameIsTaken(String email, String username) {
            crudRequestDTO.setEmail(email);
            crudRequestDTO.setUsername(username);

            when(USR_CRUD_SERVICE.update(PREDEFINED_USR.getUuid(), crudRequestDTO))
                    .thenThrow(new ConflictException("Some placeholder message"));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(UserCrudController.class.getMethod("update", UUID.class, CrudRequestDTO.class), PREDEFINED_USR.getUuid(), crudRequestDTO).toUri())
                            .content(jsonString(crudRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .update(PREDEFINED_USR.getUuid(), crudRequestDTO);
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
                            .delete(linkTo(UserCrudController.class.getMethod("delete", UUID.class), uuid).toUri())
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
                            .delete(linkTo(UserCrudController.class.getMethod("delete", UUID.class), uuid).toUri())
                    )
                    .andExpect(status().isNotFound())
                    //.andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(USR_CRUD_SERVICE)
                    .delete(uuid);
        }
    }

    private String jsonString(CrudRequestDTO crudRequestDTO) {
        try {
            return new ObjectMapper().writeValueAsString(crudRequestDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting UserDTO to json", e);
        }
    }

}

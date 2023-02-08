package com.griddynamics.internship.stonksjh.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.griddynamics.internship.stonksjh.dto.order.OrderCreateRequestDTO;
import com.griddynamics.internship.stonksjh.dto.order.OrderResponseDTO;
import com.griddynamics.internship.stonksjh.dto.order.OrderUpdateRequestDTO;
import com.griddynamics.internship.stonksjh.exception.order.InvalidOrderTypeException;
import com.griddynamics.internship.stonksjh.exception.order.InvalidStockAmountException;
import com.griddynamics.internship.stonksjh.exception.order.InvalidSymbolException;
import com.griddynamics.internship.stonksjh.exception.order.OrderNotFoundException;
import com.griddynamics.internship.stonksjh.exception.user.UserNotFoundException;
import com.griddynamics.internship.stonksjh.model.Order;
import com.griddynamics.internship.stonksjh.service.OrderService;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest(OrderController.class)
@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    private final UUID ORDER_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private final UUID OWNER_UUID = UUID.fromString("a1d82886-3f7f-41dc-9cb2-d16de3d2d287");
    @MockBean
    private OrderService ORDER_SERVICE;
    @Autowired
    private MockMvc MVC;

    private String jsonString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting UserDTO to json", e);
        }
    }

    @Nested
    class Create {

        private final Method createMethod = OrderController.class
                .getMethod("create", UUID.class, OrderCreateRequestDTO.class);

        Create() throws NoSuchMethodException {
        }

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("util.OrderFlowTestDataFactory#validOrderData")
        @SneakyThrows
        void create_OrderDataIsValid_ShouldReturnCreatedResponse(int amount, String symbol, String type) {
            val orderRequestDTO = OrderCreateRequestDTO.builder()
                    .amount(amount)
                    .symbol(symbol)
                    .type(type)
                    .build();

            when(ORDER_SERVICE.create(OWNER_UUID, requestBody))
                    .thenReturn(
                            OrderResponseDTO.builder()
                                    .uuid(ORDER_UUID)
                                    .amount(requestBody.amount())
                                    .symbol(Order.Symbol.valueOf(requestBody.symbol()))
                                    .type(Order.Type.valueOf(requestBody.type()))
                                    .build()
                    );

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(createMethod, OWNER_UUID, requestBody).toUri())
                            .content(jsonString(requestBody))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.uuid").value(ORDER_UUID.toString()));

            verify(ORDER_SERVICE)
                    .create(OWNER_UUID, requestBody);
        }

        @ParameterizedTest(name = "{index}: amount={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidAmounts")
        @SneakyThrows
        void create_OrderStockAmountIsInvalid_ShouldReturnBadResponse(int amount) {
            val orderRequestDTO = OrderCreateRequestDTO.builder()
                    .amount(amount)
                    .symbol("AAPL")
                    .type("BUY")
                    .build();

            when(ORDER_SERVICE.create(OWNER_UUID, requestBody))
                    .thenThrow(new InvalidStockAmountException(amount));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(createMethod, OWNER_UUID, requestBody).toUri())
                            .content(jsonString(requestBody))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .create(OWNER_UUID, requestBody);
        }

        @ParameterizedTest(name = "{index}: type={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidSymbolsOrTypes")
        @SneakyThrows
        void create_OrderSymbolIsInvalid_ShouldReturnBadResponse(String symbol) {
            val orderRequestDTO = OrderCreateRequestDTO.builder()
                    .amount(1)
                    .symbol(symbol)
                    .type("BUY")
                    .build();

            when(ORDER_SERVICE.create(OWNER_UUID, requestBody))
                    .thenThrow(new InvalidSymbolException(symbol));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(createMethod, OWNER_UUID, requestBody).toUri())
                            .content(jsonString(requestBody))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .create(OWNER_UUID, requestBody);
        }

        @ParameterizedTest(name = "{index}: type={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidSymbolsOrTypes")
        @SneakyThrows
        void create_OrderTypeIsInvalid_ShouldReturnBadResponse(String type) {
            val orderRequestDTO = OrderCreateRequestDTO.builder()
                    .amount(1)
                    .symbol("AAPL")
                    .type(type)
                    .build();

            when(ORDER_SERVICE.create(OWNER_UUID, requestBody))
                    .thenThrow(new InvalidOrderTypeException(type));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(createMethod, OWNER_UUID, requestBody).toUri())
                            .content(jsonString(requestBody))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .create(OWNER_UUID, requestBody);
        }

        @Test
        @SneakyThrows
        void create_shouldReturnNotFound_whenOwnerDoesNotExist() {
            val requestBody = OrderCreateRequestDTO.builder()
                    .amount(1)
                    .symbol(Order.Symbol.AAPL.toString())
                    .type(Order.Type.BUY.toString())
                    .build();

            when(ORDER_SERVICE.create(OWNER_UUID, requestBody))
                    .thenThrow(new UserNotFoundException(OWNER_UUID));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(createMethod, OWNER_UUID, requestBody).toUri())
                            .content(jsonString(requestBody))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .create(OWNER_UUID, requestBody);
        }

    }

    @Nested
    class Read {

        private final Method readOneMethod = OrderController.class
                .getMethod("read", UUID.class, UUID.class);
        private final Method readAllMethod = OrderController.class
                .getMethod("read", UUID.class);

        Read() throws NoSuchMethodException {
        }

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("util.OrderFlowTestDataFactory#validOrderData")
        @SneakyThrows
        void readOne_OrderExists_ShouldReturnOkResponse(int amount, String symbol, String type) {
            val orderRequestDTO = OrderCreateRequestDTO.builder()
                    .amount(amount)
                    .symbol(Order.Symbol.valueOf(symbol))
                    .type(Order.Type.valueOf(type))
                    .build();

            when(ORDER_SERVICE.read(OWNER_UUID, ORDER_UUID))
                    .thenReturn(
                            OrderResponseDTO.builder()
                                    .uuid(ORDER_UUID)
                                    .type(expectedResponse.type())
                                    .amount(expectedResponse.amount())
                                    .symbol(expectedResponse.symbol())
                                    .build()
                    );

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(readOneMethod, OWNER_UUID, ORDER_UUID).toUri())
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.amount").value(expectedResponse.amount()))
                    .andExpect(jsonPath("$.type").value(expectedResponse.type().toString()))
                    .andExpect(jsonPath("$.symbol").value(expectedResponse.symbol().toString()))
                    .andExpect(jsonPath("$.uuid").value(ORDER_UUID.toString()));

            verify(ORDER_SERVICE)
                    .read(OWNER_UUID, ORDER_UUID);
        }

        @Test
        @SneakyThrows
        void readOne_NoOrderWithGivenUuidExists_ShouldReturnNotFound() {
            when(ORDER_SERVICE.read(OWNER_UUID, ORDER_UUID))
                    .thenThrow(new OrderNotFoundException(ORDER_UUID));

            val expectedExceptionMessage = String.format("No order with UUID = %s exists", ORDER_UUID);

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(readOneMethod, OWNER_UUID, ORDER_UUID).toUri())
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", matchesPattern(".*" + expectedExceptionMessage + ".*")))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .read(OWNER_UUID, ORDER_UUID);
        }

        @Test
        @SneakyThrows
        void readAll_shouldReturnEmptyList_whenUserHadNoOrders() {
            when(ORDER_SERVICE.read(OWNER_UUID))
                    .thenReturn(List.of());

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(readAllMethod, OWNER_UUID).toUri())
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());

            verify(ORDER_SERVICE)
                    .read(OWNER_UUID);
        }

        @Test
        @SneakyThrows
        void readAll_shouldReturnNonEmptyList_whenUserHadOrders() {
            val expectedResponse = OrderResponseDTO.builder()
                    .amount(1)
                    .symbol(Order.Symbol.AAPL)
                    .type(Order.Type.BUY)
                    .build();

            when(ORDER_SERVICE.read(OWNER_UUID))
                    .thenReturn(List.of(expectedResponse));

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(readAllMethod, OWNER_UUID).toUri())
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isNotEmpty())
                    .andExpect(jsonPath("$[0].amount").value(expectedResponse.amount()))
                    .andExpect(jsonPath("$[0].symbol").value(expectedResponse.symbol().toString()))
                    .andExpect(jsonPath("$[0].type").value(expectedResponse.type().toString()));

            verify(ORDER_SERVICE)
                    .read(OWNER_UUID);
        }

        @Test
        @SneakyThrows
        void readAll_shouldReturnNotFound_whenOwnerDoesNotExist() {
            when(ORDER_SERVICE.read(OWNER_UUID))
                    .thenThrow(new UserNotFoundException(OWNER_UUID));

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(readAllMethod, OWNER_UUID).toUri())
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .read(OWNER_UUID);
        }

    }

    @Nested
    class Update {

        private final Method updateMethod = OrderController.class
                .getMethod("update", UUID.class, UUID.class, OrderUpdateRequestDTO.class);

        Update() throws NoSuchMethodException {
        }

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("util.OrderFlowTestDataFactory#validOrderData")
        @SneakyThrows
        void update_OrderDataIsValid_ShouldReturnOkResponse(int amount, String symbol, String type) {
            val orderRequestDTO = OrderUpdateRequestDTO.builder()
                    .amount(amount)
                    .symbol(symbol)
                    .build();

            when(ORDER_SERVICE.update(OWNER_UUID, ORDER_UUID, requestBody))
                    .thenReturn(
                            OrderResponseDTO.builder()
                                    .uuid(ORDER_UUID)
                                    .amount(orderRequestDTO.amount())
                                    .symbol(Order.Symbol.valueOf(orderRequestDTO.symbol()))
                                    .build()
                    );

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(updateMethod, OWNER_UUID, ORDER_UUID, requestBody).toUri())
                            .content(jsonString(requestBody))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.amount").value(requestBody.amount()))
                    .andExpect(jsonPath("$.symbol").value(requestBody.symbol()))
                    .andExpect(jsonPath("$.uuid").value(ORDER_UUID.toString()));

            verify(ORDER_SERVICE)
                    .update(OWNER_UUID, ORDER_UUID, requestBody);
        }

        @ParameterizedTest(name = "{index}: amount={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidAmounts")
        @SneakyThrows
        void update_OrderStockAmountIsInvalid_ShouldReturnBadRequest(int amount) {
            val orderRequestDTO = OrderUpdateRequestDTO.builder()
                    .amount(amount)
                    .symbol("AAPL")
                    .build();

            when(ORDER_SERVICE.update(OWNER_UUID, ORDER_UUID, requestBody))
                    .thenThrow(new InvalidStockAmountException(amount));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(updateMethod, OWNER_UUID, ORDER_UUID, requestBody).toUri())
                            .content(jsonString(requestBody))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .update(OWNER_UUID, ORDER_UUID, requestBody);
        }

        @ParameterizedTest(name = "{index}: symbol={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidSymbolsOrTypes")
        @SneakyThrows
        void update_OrderSymbolIsInvalid_ShouldReturnBadRequest(String symbol) {
            val orderRequestDTO = OrderUpdateRequestDTO.builder()
                    .amount(1)
                    .symbol(symbol)
                    .build();

            when(ORDER_SERVICE.update(OWNER_UUID, ORDER_UUID, requestBody))
                    .thenThrow(new InvalidSymbolException(symbol));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(updateMethod, OWNER_UUID, ORDER_UUID, requestBody).toUri())
                            .content(jsonString(requestBody))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .update(OWNER_UUID, ORDER_UUID, requestBody);
        }

        @ParameterizedTest(name = "{index}: type={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidSymbolsOrTypes")
        @SneakyThrows
        void update_OrderTypeIsInvalid_ShouldReturnBadRequest(String type) {
            val orderRequestDTO = OrderUpdateRequestDTO.builder()
                    .amount(1)
                    .symbol("AAPL")
                    .build();

            when(ORDER_SERVICE.update(OWNER_UUID, ORDER_UUID, requestBody))
                    .thenThrow(new InvalidOrderTypeException(type));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(updateMethod, OWNER_UUID, ORDER_UUID, requestBody).toUri())
                            .content(jsonString(requestBody))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .update(OWNER_UUID, ORDER_UUID, requestBody);
        }

    }

    @Nested
    class Delete {

        private final Method deleteMethod = OrderController.class
                .getMethod("delete", UUID.class, UUID.class);

        Delete() throws NoSuchMethodException {
        }

        @Test
        @SneakyThrows
        void delete_OrderExists_ShouldReturnOkResponse() {
            doNothing()
                    .when(ORDER_SERVICE)
                    .delete(OWNER_UUID, ORDER_UUID);

            MVC.perform(MockMvcRequestBuilders
                            .delete(linkTo(deleteMethod, OWNER_UUID, ORDER_UUID).toUri())
                    )
                    .andExpect(status().isOk());

            verify(ORDER_SERVICE)
                    .delete(OWNER_UUID, ORDER_UUID);
        }

        @Test
        @SneakyThrows
        void delete_NoOrderWithGivenUuidExists_ShouldReturnNotFound() {
            doThrow(new OrderNotFoundException(ORDER_UUID))
                    .when(ORDER_SERVICE).delete(OWNER_UUID, ORDER_UUID);

            val expectedExceptionMessage = String.format("No order with UUID = %s exists", ORDER_UUID);

            MVC.perform(MockMvcRequestBuilders
                            .delete(linkTo(deleteMethod, OWNER_UUID, ORDER_UUID).toUri())
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", matchesPattern(".*" + expectedExceptionMessage + ".*")))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .delete(OWNER_UUID, ORDER_UUID);
        }

    }

}

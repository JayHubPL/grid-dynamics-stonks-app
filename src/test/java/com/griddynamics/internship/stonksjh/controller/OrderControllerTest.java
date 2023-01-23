package com.griddynamics.internship.stonksjh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.griddynamics.internship.stonksjh.dto.order.OrderRequestDTO;
import com.griddynamics.internship.stonksjh.dto.order.OrderResponseDTO;
import com.griddynamics.internship.stonksjh.exception.order.InvalidOrderTypeException;
import com.griddynamics.internship.stonksjh.exception.order.InvalidStockAmountException;
import com.griddynamics.internship.stonksjh.exception.order.InvalidSymbolException;
import com.griddynamics.internship.stonksjh.exception.order.OrderNotFoundException;
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

    private static final UUID VALID_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    @MockBean
    private OrderService ORDER_SERVICE;
    @Autowired
    private MockMvc MVC;

    @Nested
    class Create {

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("util.OrderFlowTestDataFactory#validOrderData")
        @SneakyThrows
        void create_OrderDataIsValid_ShouldReturnCreatedResponse(int amount, String symbol, String type) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(amount)
                    .symbol(symbol)
                    .type(type)
                    .build();

            when(ORDER_SERVICE.create(orderRequestDTO)).thenReturn(new OrderResponseDTO(
                    VALID_UUID,
                    Order.Type.valueOf(orderRequestDTO.type()),
                    orderRequestDTO.amount(),
                    Order.Symbol.valueOf(orderRequestDTO.symbol())
            ));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(OrderController.class.getMethod("create", OrderRequestDTO.class), orderRequestDTO).toUri())
                            .content(new ObjectMapper().writeValueAsString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                    ).andExpect(status().isCreated())
                    .andExpect(jsonPath("$.uuid").value(VALID_UUID.toString()));

            verify(ORDER_SERVICE).create(orderRequestDTO);
        }

        @ParameterizedTest(name = "{index}: amount={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidAmounts")
        @SneakyThrows
        void create_OrderStockAmountIsInvalid_ShouldReturnBadResponse(int amount) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(amount)
                    .symbol("AAPL")
                    .type("BUY")
                    .build();

            when(ORDER_SERVICE.create(orderRequestDTO))
                    .thenThrow(new InvalidStockAmountException(amount));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(OrderController.class.getMethod("create", OrderRequestDTO.class), orderRequestDTO).toUri())
                            .content(new ObjectMapper().writeValueAsString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE).create(orderRequestDTO);
        }

        @ParameterizedTest(name = "{index}: type={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidSymbolsOrTypes")
        @SneakyThrows
        void create_OrderSymbolIsInvalid_ShouldReturnBadResponse(String symbol) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(1)
                    .symbol(symbol)
                    .type("BUY")
                    .build();

            when(ORDER_SERVICE.create(orderRequestDTO))
                    .thenThrow(new InvalidSymbolException(symbol));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(OrderController.class.getMethod("create", OrderRequestDTO.class), orderRequestDTO).toUri())
                            .content(new ObjectMapper().writeValueAsString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE).create(orderRequestDTO);
        }

        @ParameterizedTest(name = "{index}: type={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidSymbolsOrTypes")
        @SneakyThrows
        void create_OrderTypeIsInvalid_ShouldReturnBadResponse(String type) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(1)
                    .symbol("AAPL")
                    .type(type)
                    .build();

            when(ORDER_SERVICE.create(orderRequestDTO))
                    .thenThrow(new InvalidOrderTypeException(type));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(OrderController.class.getMethod("create", OrderRequestDTO.class), orderRequestDTO).toUri())
                            .content(new ObjectMapper().writeValueAsString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE).create(orderRequestDTO);
        }

    }

    @Nested
    class Read {

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("util.OrderFlowTestDataFactory#validOrderData")
        @SneakyThrows
        void readOne_OrderExists_ShouldReturnOkResponse(int amount, String symbol, String type) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(amount)
                    .symbol(symbol)
                    .type(type)
                    .build();

            when(ORDER_SERVICE.read(VALID_UUID)).thenReturn(new OrderResponseDTO(
                    VALID_UUID,
                    Order.Type.valueOf(orderRequestDTO.type()),
                    orderRequestDTO.amount(),
                    Order.Symbol.valueOf(orderRequestDTO.symbol())
            ));

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(OrderController.class.getMethod("read", UUID.class), VALID_UUID).toUri())
                    ).andExpect(status().isOk())
                    .andExpect(jsonPath("$.amount").value(orderRequestDTO.amount()))
                    .andExpect(jsonPath("$.type").value(orderRequestDTO.type()))
                    .andExpect(jsonPath("$.symbol").value(orderRequestDTO.symbol()))
                    .andExpect(jsonPath("$.uuid").value(VALID_UUID.toString()));

            verify(ORDER_SERVICE).read(VALID_UUID);
        }

        @Test
        @SneakyThrows
        void readOne_UuidIsOfInvalidFormat_ShouldReturnBadRequest() {
            val uuidString = "aaa";
            val expectedExceptionMessage = "Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; "
                    + "Invalid UUID string: " + uuidString;

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(OrderController.class.getMethod("read", UUID.class), uuidString).toUri())
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", matchesPattern(".*" + expectedExceptionMessage + ".*")))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @SneakyThrows
        void readOne_NoOrderWithGivenUuidExists_ShouldReturnNotFound() {
            when(ORDER_SERVICE.read(VALID_UUID)).thenThrow(new OrderNotFoundException(VALID_UUID));

            val expectedExceptionMessage = String.format("No order with UUID = %s exists", VALID_UUID);

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(OrderController.class.getMethod("read", UUID.class), VALID_UUID).toUri())
                    ).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", matchesPattern(".*" + expectedExceptionMessage + ".*")))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE).read(VALID_UUID);
        }
    }

    @Nested
    class Update {

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("util.OrderFlowTestDataFactory#validOrderData")
        @SneakyThrows
        void update_OrderDataIsValid_ShouldReturnOkResponse(int amount, String symbol, String type) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(amount)
                    .symbol(symbol)
                    .type(type)
                    .build();

            when(ORDER_SERVICE.update(VALID_UUID, orderRequestDTO)).thenReturn(new OrderResponseDTO(
                    VALID_UUID,
                    Order.Type.valueOf(orderRequestDTO.type()),
                    orderRequestDTO.amount(),
                    Order.Symbol.valueOf(orderRequestDTO.symbol())
            ));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(OrderController.class.getMethod("update", UUID.class, OrderRequestDTO.class),
                                    VALID_UUID, orderRequestDTO).toUri())
                            .content(new ObjectMapper().writeValueAsString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    ).andExpect(status().isOk())
                    .andExpect(jsonPath("$.amount").value(orderRequestDTO.amount()))
                    .andExpect(jsonPath("$.symbol").value(orderRequestDTO.symbol()))
                    .andExpect(jsonPath("$.uuid").value(VALID_UUID.toString()));

            verify(ORDER_SERVICE).update(VALID_UUID, orderRequestDTO);
        }

        @ParameterizedTest(name = "{index}: amount={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidAmounts")
        @SneakyThrows
        void update_OrderStockAmountIsInvalid_ShouldReturnBadRequest(int amount) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(amount)
                    .symbol("AAPL")
                    .type("BUY")
                    .build();

            when(ORDER_SERVICE.update(VALID_UUID, orderRequestDTO))
                    .thenThrow(new InvalidStockAmountException(amount));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(OrderController.class.getMethod("update", UUID.class, OrderRequestDTO.class),
                                    VALID_UUID, orderRequestDTO).toUri())
                            .content(new ObjectMapper().writeValueAsString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @ParameterizedTest(name = "{index}: symbol={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidSymbolsOrTypes")
        @SneakyThrows
        void update_OrderSymbolIsInvalid_ShouldReturnBadRequest(String symbol) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(1)
                    .symbol(symbol)
                    .type("BUY")
                    .build();

            when(ORDER_SERVICE.update(VALID_UUID, orderRequestDTO))
                    .thenThrow(new InvalidSymbolException(symbol));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(OrderController.class.getMethod("update", UUID.class, OrderRequestDTO.class),
                                    VALID_UUID, orderRequestDTO).toUri())
                            .content(new ObjectMapper().writeValueAsString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @ParameterizedTest(name = "{index}: type={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidSymbolsOrTypes")
        @SneakyThrows
        void update_OrderTypeIsInvalid_ShouldReturnBadRequest(String type) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(1)
                    .symbol("AAPL")
                    .type(type)
                    .build();

            when(ORDER_SERVICE.update(VALID_UUID, orderRequestDTO))
                    .thenThrow(new InvalidOrderTypeException(type));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(OrderController.class.getMethod("update", UUID.class, OrderRequestDTO.class),
                                    VALID_UUID, orderRequestDTO).toUri())
                            .content(new ObjectMapper().writeValueAsString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

    }

    @Nested
    class Delete {

        @Test
        @SneakyThrows
        void delete_OrderExists_ShouldReturnOkResponse() {
            doNothing().when(ORDER_SERVICE).delete(VALID_UUID);

            MVC.perform(MockMvcRequestBuilders
                    .delete(linkTo(OrderController.class.getMethod("delete", UUID.class), VALID_UUID).toUri())
            ).andExpect(status().isOk());

            verify(ORDER_SERVICE).delete(VALID_UUID);
        }

        @Test
        @SneakyThrows
        void delete_NoOrderWithGivenUuidExists_ShouldReturnNotFound() {
            doThrow(new OrderNotFoundException(VALID_UUID)).when(ORDER_SERVICE).delete(VALID_UUID);

            val expectedExceptionMessage = String.format("No order with UUID = %s exists", VALID_UUID);

            MVC.perform(MockMvcRequestBuilders
                            .delete(linkTo(OrderController.class.getMethod("delete", UUID.class), VALID_UUID).toUri())
                    ).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", matchesPattern(".*" + expectedExceptionMessage + ".*")))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE).delete(VALID_UUID);
        }

    }

}

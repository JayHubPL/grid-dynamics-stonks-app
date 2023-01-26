package com.griddynamics.internship.stonksjh.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.lang.reflect.Method;
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

    private String jsonString(OrderRequestDTO orderRequestDTO) {
        try {
            return new ObjectMapper().writeValueAsString(orderRequestDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting UserDTO to json", e);
        }
    }

    @Nested
    class Create {

        private final Method createMethod = OrderController.class
                .getMethod("create", UUID.class, OrderRequestDTO.class);

        Create() throws NoSuchMethodException {
        }

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("util.OrderFlowTestDataFactory#validOrderData")
        @SneakyThrows
        void create_OrderDataIsValid_ShouldReturnCreatedResponse(int amount, String symbol, String type) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(amount)
                    .symbol(symbol)
                    .type(type)
                    .build();

            when(ORDER_SERVICE.create(OWNER_UUID, orderRequestDTO))
                    .thenReturn(
                            OrderResponseDTO.builder()
                                    .uuid(ORDER_UUID)
                                    .amount(orderRequestDTO.amount())
                                    .symbol(Order.Symbol.valueOf(orderRequestDTO.symbol()))
                                    .type(Order.Type.valueOf(orderRequestDTO.type()))
                                    .build()
                    );

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(createMethod, OWNER_UUID, orderRequestDTO).toUri())
                            .content(jsonString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.uuid").value(ORDER_UUID.toString()));

            verify(ORDER_SERVICE)
                    .create(OWNER_UUID, orderRequestDTO);
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

            when(ORDER_SERVICE.create(OWNER_UUID, orderRequestDTO))
                    .thenThrow(new InvalidStockAmountException(amount));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(createMethod, OWNER_UUID, orderRequestDTO).toUri())
                            .content(jsonString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .create(OWNER_UUID, orderRequestDTO);
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

            when(ORDER_SERVICE.create(OWNER_UUID, orderRequestDTO))
                    .thenThrow(new InvalidSymbolException(symbol));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(createMethod, OWNER_UUID, orderRequestDTO).toUri())
                            .content(jsonString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .create(OWNER_UUID, orderRequestDTO);
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

            when(ORDER_SERVICE.create(OWNER_UUID, orderRequestDTO))
                    .thenThrow(new InvalidOrderTypeException(type));

            MVC.perform(MockMvcRequestBuilders
                            .post(linkTo(createMethod, OWNER_UUID, orderRequestDTO).toUri())
                            .content(jsonString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .create(OWNER_UUID, orderRequestDTO);
        }

    }

    @Nested
    class Read {

        private final Method readMethod = OrderController.class
                .getMethod("read", UUID.class, UUID.class);

        Read() throws NoSuchMethodException {
        }

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("util.OrderFlowTestDataFactory#validOrderData")
        @SneakyThrows
        void readOne_OrderExists_ShouldReturnOkResponse(int amount, String symbol, String type) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(amount)
                    .symbol(symbol)
                    .type(type)
                    .build();

            when(ORDER_SERVICE.read(OWNER_UUID, ORDER_UUID))
                    .thenReturn(
                            OrderResponseDTO.builder()
                                    .uuid(ORDER_UUID)
                                    .type(Order.Type.valueOf(orderRequestDTO.type()))
                                    .amount(orderRequestDTO.amount())
                                    .symbol(Order.Symbol.valueOf(orderRequestDTO.symbol()))
                                    .build()
                    );

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(readMethod, OWNER_UUID, ORDER_UUID).toUri())
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.amount").value(orderRequestDTO.amount()))
                    .andExpect(jsonPath("$.type").value(orderRequestDTO.type()))
                    .andExpect(jsonPath("$.symbol").value(orderRequestDTO.symbol()))
                    .andExpect(jsonPath("$.uuid").value(ORDER_UUID.toString()));

            verify(ORDER_SERVICE)
                    .read(OWNER_UUID, ORDER_UUID);
        }

        @Test
        @SneakyThrows
        void readOne_UuidIsOfInvalidFormat_ShouldReturnBadRequest() {
            val uuidString = "aaa";
            val expectedExceptionMessage = "Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; "
                    + "Invalid UUID string: " + uuidString;

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(readMethod, OWNER_UUID, uuidString).toUri())
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", matchesPattern(".*" + expectedExceptionMessage + ".*")))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @SneakyThrows
        void readOne_NoOrderWithGivenUuidExists_ShouldReturnNotFound() {
            when(ORDER_SERVICE.read(OWNER_UUID, ORDER_UUID))
                    .thenThrow(new OrderNotFoundException(ORDER_UUID));

            val expectedExceptionMessage = String.format("No order with UUID = %s exists", ORDER_UUID);

            MVC.perform(MockMvcRequestBuilders
                            .get(linkTo(readMethod, OWNER_UUID, ORDER_UUID).toUri())
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", matchesPattern(".*" + expectedExceptionMessage + ".*")))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .read(OWNER_UUID, ORDER_UUID);
        }
    }

    @Nested
    class Update {

        private final Method updateMethod = OrderController.class
                .getMethod("update", UUID.class, UUID.class, OrderRequestDTO.class);

        Update() throws NoSuchMethodException {
        }

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("util.OrderFlowTestDataFactory#validOrderData")
        @SneakyThrows
        void update_OrderDataIsValid_ShouldReturnOkResponse(int amount, String symbol, String type) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(amount)
                    .symbol(symbol)
                    .type(type)
                    .build();

            when(ORDER_SERVICE.update(OWNER_UUID, ORDER_UUID, orderRequestDTO))
                    .thenReturn(
                            OrderResponseDTO.builder()
                                    .uuid(ORDER_UUID)
                                    .type(Order.Type.valueOf(orderRequestDTO.type()))
                                    .amount(orderRequestDTO.amount())
                                    .symbol(Order.Symbol.valueOf(orderRequestDTO.symbol()))
                                    .build()
                    );

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(updateMethod, OWNER_UUID, ORDER_UUID, orderRequestDTO).toUri())
                            .content(jsonString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.amount").value(orderRequestDTO.amount()))
                    .andExpect(jsonPath("$.symbol").value(orderRequestDTO.symbol()))
                    .andExpect(jsonPath("$.uuid").value(ORDER_UUID.toString()));

            verify(ORDER_SERVICE)
                    .update(OWNER_UUID, ORDER_UUID, orderRequestDTO);
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

            when(ORDER_SERVICE.update(OWNER_UUID, ORDER_UUID, orderRequestDTO))
                    .thenThrow(new InvalidStockAmountException(amount));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(updateMethod, OWNER_UUID, ORDER_UUID, orderRequestDTO).toUri())
                            .content(jsonString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .update(OWNER_UUID, ORDER_UUID, orderRequestDTO);
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

            when(ORDER_SERVICE.update(OWNER_UUID, ORDER_UUID, orderRequestDTO))
                    .thenThrow(new InvalidSymbolException(symbol));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(updateMethod, OWNER_UUID, ORDER_UUID, orderRequestDTO).toUri())
                            .content(jsonString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .update(OWNER_UUID, ORDER_UUID, orderRequestDTO);
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

            when(ORDER_SERVICE.update(OWNER_UUID, ORDER_UUID, orderRequestDTO))
                    .thenThrow(new InvalidOrderTypeException(type));

            MVC.perform(MockMvcRequestBuilders
                            .put(linkTo(updateMethod, OWNER_UUID, ORDER_UUID, orderRequestDTO).toUri())
                            .content(jsonString(orderRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(ORDER_SERVICE)
                    .update(OWNER_UUID, ORDER_UUID, orderRequestDTO);
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

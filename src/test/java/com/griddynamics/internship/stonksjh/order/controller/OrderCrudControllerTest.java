package com.griddynamics.internship.stonksjh.order.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.griddynamics.internship.stonksjh.order.dto.OrderDTO;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.InvalidStockAmountException;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.OrderNotFoundException;
import com.griddynamics.internship.stonksjh.order.model.OrderType;
import com.griddynamics.internship.stonksjh.order.service.OrderCrudService;

import lombok.SneakyThrows;
import lombok.val;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest(OrderCrudController.class)
@ExtendWith(MockitoExtension.class)
public class OrderCrudControllerTest {

    private static final String LOCALHOST = "http://localhost";
    public static final String UUID_REGEX =
        "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";
    private static final UUID VALID_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    OrderCrudService mockedOrderService;

    private OrderDTO orderDTO;

    @BeforeEach
    void initOrderDTO() {
        orderDTO = new OrderDTO();
    }

    @Nested
    class Create {

        @ParameterizedTest(name = "{index}: orderData=[{0},{1}]")
        @MethodSource("com.griddynamics.internship.stonksjh.order.controller.util.TestDataFactory#validOrderData")
        @SneakyThrows
        void create_OrderDataIsValid_ShouldReturnCreatedResponse(int amount, String symbol) {
            orderDTO.setAmount(amount);
            orderDTO.setSymbol(symbol);

            when(mockedOrderService.createOrder(orderDTO)).thenReturn(new OrderDTO(
                VALID_UUID,
                orderDTO.getOrderType(),
                orderDTO.getAmount(),
                orderDTO.getSymbol()
            ));

            val response = mockMvc.perform(MockMvcRequestBuilders
                .post(linkTo(OrderCrudController.class.getMethod("create", OrderDTO.class), orderDTO).toUri())
                .content(new ObjectMapper().writeValueAsString(orderDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isCreated())
            .andReturn()
            .getResponse();

            assertThat(response.getHeader("Location"))
                .matches(LOCALHOST + linkTo(OrderCrudController.class.getMethod("read", UUID.class), "") + UUID_REGEX + "$");

            verify(mockedOrderService).createOrder(orderDTO);
        }

        @ParameterizedTest(name = "{index}: orderData=[{0},{1}]")
        @MethodSource("com.griddynamics.internship.stonksjh.order.controller.util.TestDataFactory#invalidOrderData")
        @SneakyThrows
        void create_OrderDataIsInvalid_ShouldReturnBadResponse(int amount, String symbol) {
            orderDTO.setAmount(amount);
            orderDTO.setSymbol(symbol);

            when(mockedOrderService.createOrder(orderDTO)).thenThrow(new InvalidStockAmountException(amount));

            mockMvc.perform(MockMvcRequestBuilders
                .post(linkTo(OrderCrudController.class.getMethod("create", OrderDTO.class), orderDTO).toUri())
                .content(new ObjectMapper().writeValueAsString(orderDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").isNotEmpty())
            .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(mockedOrderService).createOrder(orderDTO);
        }

    }

    @Nested
    class Read {

        @Test
        @SneakyThrows
        void readOne_OrderExists_ShouldReturnOkResponse() {
            val uuid = VALID_UUID;
            orderDTO.setAmount(1);
            orderDTO.setOrderType(OrderType.BUY);
            orderDTO.setSymbol("AAPL");
            orderDTO.setUuid(uuid);

            when(mockedOrderService.readOneOrder(uuid)).thenReturn(orderDTO);

            mockMvc.perform(MockMvcRequestBuilders
                .get(linkTo(OrderCrudController.class.getMethod("read", UUID.class), uuid).toUri())
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.amount").value(orderDTO.getAmount()))
            .andExpect(jsonPath("$.orderType").value(orderDTO.getOrderType().name()))
            .andExpect(jsonPath("$.symbol").value(orderDTO.getSymbol()))
            .andExpect(jsonPath("$.uuid").value(orderDTO.getUuid().toString()));

            verify(mockedOrderService).readOneOrder(uuid);
        }

        @Test
        @SneakyThrows
        void readOne_UuidIsOfInvalidFormat_ShouldReturnBadRequest() {
            val uuidString = "aaa";
            val expectedExceptionMessage = "Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; "
                + "Invalid UUID string: " + uuidString;

            mockMvc.perform(MockMvcRequestBuilders
                .get(linkTo(OrderCrudController.class.getMethod("read", UUID.class), uuidString).toUri())
            ).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", matchesPattern(".*" + expectedExceptionMessage + ".*")))
            .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @SneakyThrows
        void readOne_NoOrderWithGivenUuidExists_ShouldReturnNotFound() {
            when(mockedOrderService.readOneOrder(VALID_UUID)).thenThrow(new OrderNotFoundException(VALID_UUID));

            val expectedExceptionMessage = String.format("No order with UUID = %s exists", VALID_UUID);

            mockMvc.perform(MockMvcRequestBuilders
                .get(linkTo(OrderCrudController.class.getMethod("read", UUID.class), VALID_UUID).toUri())
            ).andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", matchesPattern(".*" + expectedExceptionMessage + ".*")))
            .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(mockedOrderService).readOneOrder(VALID_UUID);
        }
    }

    @Nested
    class Update {

        @ParameterizedTest
        @MethodSource("com.griddynamics.internship.stonksjh.order.controller.util.TestDataFactory#validOrderData")
        @SneakyThrows
        void update_OrderDataIsValid_ShouldReturnOkResponse(int amount, String symbol) {
            orderDTO.setUuid(VALID_UUID);
            orderDTO.setAmount(amount);
            orderDTO.setSymbol(symbol);

            when(mockedOrderService.updateOrder(VALID_UUID, orderDTO)).thenReturn(orderDTO);

            mockMvc.perform(MockMvcRequestBuilders
                .put(linkTo(OrderCrudController.class.getMethod("update", UUID.class, OrderDTO.class), VALID_UUID, orderDTO).toUri())
                .content(new ObjectMapper().writeValueAsString(orderDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.amount").value(orderDTO.getAmount()))
            .andExpect(jsonPath("$.symbol").value(orderDTO.getSymbol()))
            .andExpect(jsonPath("$.uuid").value(VALID_UUID.toString()));

            verify(mockedOrderService).updateOrder(VALID_UUID, orderDTO);
        }

        @ParameterizedTest
        @MethodSource("com.griddynamics.internship.stonksjh.order.controller.util.TestDataFactory#invalidOrderData")
        @SneakyThrows
        void update_OrderDataIsInvalid_ShouldReturnBadRequest(int amount, String symbol) {
            orderDTO.setAmount(amount);
            orderDTO.setSymbol(symbol);

            val expectedExceptionMessage = String.format("Stock amount cannot be nonpositive, was %d", amount);

            when(mockedOrderService.updateOrder(VALID_UUID, orderDTO)).thenThrow(new InvalidStockAmountException(amount));

            mockMvc.perform(MockMvcRequestBuilders
                .put(linkTo(OrderCrudController.class.getMethod("update", UUID.class, OrderDTO.class), VALID_UUID, orderDTO).toUri())
                .content(new ObjectMapper().writeValueAsString(orderDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", matchesPattern(".*" + expectedExceptionMessage + ".*")))
            .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

    }

    @Nested
    class Delete {

        @Test
        @SneakyThrows
        void delete_OrderExists_ShouldReturnOkResponse() {
            doNothing().when(mockedOrderService).deleteOrder(VALID_UUID);

            mockMvc.perform(MockMvcRequestBuilders
                .delete(linkTo(OrderCrudController.class.getMethod("delete", UUID.class), VALID_UUID).toUri())
            ).andExpect(status().isOk());

            verify(mockedOrderService).deleteOrder(VALID_UUID);
        }

        @Test
        @SneakyThrows
        void delete_NoOrderWithGivenUuidExists_ShouldReturnNotFound() {
            doThrow(new OrderNotFoundException(VALID_UUID)).when(mockedOrderService).deleteOrder(VALID_UUID);

            val expectedExceptionMessage = String.format("No order with UUID = %s exists", VALID_UUID);

            mockMvc.perform(MockMvcRequestBuilders
                .delete(linkTo(OrderCrudController.class.getMethod("delete", UUID.class), VALID_UUID).toUri())
            ).andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", matchesPattern(".*" + expectedExceptionMessage + ".*")))
            .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(mockedOrderService).deleteOrder(VALID_UUID);
        }

    }

}

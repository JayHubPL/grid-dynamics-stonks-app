package com.griddynamics.internship.stonksjh.order.controller;

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
import com.griddynamics.internship.stonksjh.order.dto.CrudRequestDTO;
import com.griddynamics.internship.stonksjh.order.dto.OrderDTO;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.InvalidStockAmountException;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.OrderNotFoundException;
import com.griddynamics.internship.stonksjh.order.model.OrderType;
import com.griddynamics.internship.stonksjh.order.model.Symbol;
import com.griddynamics.internship.stonksjh.order.service.OrderCrudService;

import lombok.SneakyThrows;
import lombok.val;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest(OrderCrudController.class)
@ExtendWith(MockitoExtension.class)
public class OrderCrudControllerTest {

    public static final String UUID_REGEX =
        "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";
    private static final UUID VALID_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    OrderCrudService mockedOrderService;

    private CrudRequestDTO crudRequestDTO;

    @BeforeEach
    void initCrudRequestDTO() {
        crudRequestDTO = new CrudRequestDTO();
    }

    @Nested
    class Create {

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("com.griddynamics.internship.stonksjh.util.TestDataFactory#validOrderData")
        @SneakyThrows
        void create_OrderDataIsValid_ShouldReturnCreatedResponse(int amount, String symbol, String orderType) {
            crudRequestDTO.setAmount(amount);
            crudRequestDTO.setSymbol(symbol);
            crudRequestDTO.setOrderType(orderType);

            when(mockedOrderService.createOrder(crudRequestDTO)).thenReturn(new OrderDTO(
                VALID_UUID,
                OrderType.valueOf(crudRequestDTO.getOrderType()),
                crudRequestDTO.getAmount(),
                Symbol.valueOf(crudRequestDTO.getSymbol())
            ));

            mockMvc.perform(MockMvcRequestBuilders
                .post(linkTo(OrderCrudController.class.getMethod("create", CrudRequestDTO.class), crudRequestDTO).toUri())
                .content(new ObjectMapper().writeValueAsString(crudRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isCreated())
            .andExpect(jsonPath("$.uuid").value(VALID_UUID.toString()));

            verify(mockedOrderService).createOrder(crudRequestDTO);
        }

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("com.griddynamics.internship.stonksjh.util.TestDataFactory#invalidOrderData")
        @SneakyThrows
        void create_OrderDataIsInvalid_ShouldReturnBadResponse(int amount, String symbol, String orderType) {
            crudRequestDTO.setAmount(amount);
            crudRequestDTO.setSymbol(symbol);
            crudRequestDTO.setOrderType(orderType);

            when(mockedOrderService.createOrder(crudRequestDTO)).thenThrow(new InvalidStockAmountException(amount));

            mockMvc.perform(MockMvcRequestBuilders
                .post(linkTo(OrderCrudController.class.getMethod("create", CrudRequestDTO.class), crudRequestDTO).toUri())
                .content(new ObjectMapper().writeValueAsString(crudRequestDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").isNotEmpty())
            .andExpect(jsonPath("$.timestamp").isNotEmpty());

            verify(mockedOrderService).createOrder(crudRequestDTO);
        }

    }

    @Nested
    class Read {

        @Test
        @SneakyThrows
        void readOne_OrderExists_ShouldReturnOkResponse() {
            crudRequestDTO.setAmount(1);
            crudRequestDTO.setOrderType("BUY");
            crudRequestDTO.setSymbol("AAPL");

            when(mockedOrderService.readOneOrder(VALID_UUID)).thenReturn(new OrderDTO(
                VALID_UUID,
                OrderType.valueOf(crudRequestDTO.getOrderType()),
                crudRequestDTO.getAmount(),
                Symbol.valueOf(crudRequestDTO.getSymbol())
            ));

            mockMvc.perform(MockMvcRequestBuilders
                .get(linkTo(OrderCrudController.class.getMethod("read", UUID.class), VALID_UUID).toUri())
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.amount").value(crudRequestDTO.getAmount()))
            .andExpect(jsonPath("$.orderType").value(crudRequestDTO.getOrderType()))
            .andExpect(jsonPath("$.symbol").value(crudRequestDTO.getSymbol()))
            .andExpect(jsonPath("$.uuid").value(VALID_UUID.toString()));

            verify(mockedOrderService).readOneOrder(VALID_UUID);
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

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("com.griddynamics.internship.stonksjh.util.TestDataFactory#validOrderData")
        @SneakyThrows
        void update_OrderDataIsValid_ShouldReturnOkResponse(int amount, String symbol, String orderType) {
            crudRequestDTO.setAmount(amount);
            crudRequestDTO.setSymbol(symbol);
            crudRequestDTO.setOrderType(orderType);

            when(mockedOrderService.updateOrder(VALID_UUID, crudRequestDTO)).thenReturn(new OrderDTO(
                VALID_UUID,
                OrderType.valueOf(crudRequestDTO.getOrderType()),
                crudRequestDTO.getAmount(),
                Symbol.valueOf(crudRequestDTO.getSymbol())
            ));

            mockMvc.perform(MockMvcRequestBuilders
                .put(linkTo(OrderCrudController.class.getMethod("update", UUID.class, CrudRequestDTO.class),
                    VALID_UUID, crudRequestDTO).toUri())
                .content(new ObjectMapper().writeValueAsString(crudRequestDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.amount").value(crudRequestDTO.getAmount()))
            .andExpect(jsonPath("$.symbol").value(crudRequestDTO.getSymbol()))
            .andExpect(jsonPath("$.uuid").value(VALID_UUID.toString()));

            verify(mockedOrderService).updateOrder(VALID_UUID, crudRequestDTO);
        }

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("com.griddynamics.internship.stonksjh.util.TestDataFactory#invalidOrderData")
        @SneakyThrows
        void update_OrderDataIsInvalid_ShouldReturnBadRequest(int amount, String symbol, String orderType) {
            crudRequestDTO.setAmount(amount);
            crudRequestDTO.setSymbol(symbol);
            crudRequestDTO.setOrderType(orderType);

            val expectedExceptionMessage = String.format("Stock amount cannot be nonpositive, was %d", amount);

            when(mockedOrderService.updateOrder(VALID_UUID, crudRequestDTO)).thenThrow(new InvalidStockAmountException(amount));

            mockMvc.perform(MockMvcRequestBuilders
                .put(linkTo(OrderCrudController.class.getMethod("update", UUID.class, CrudRequestDTO.class),
                    VALID_UUID, crudRequestDTO).toUri())
                .content(new ObjectMapper().writeValueAsString(crudRequestDTO))
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

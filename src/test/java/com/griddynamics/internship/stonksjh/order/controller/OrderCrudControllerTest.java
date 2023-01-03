package com.griddynamics.internship.stonksjh.order.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
        void create_OrderDataIsValid_ShouldReturn201(int amount, String symbol) {
            orderDTO.setAmount(amount);
            orderDTO.setSymbol(symbol);

            when(mockedOrderService.createOrder(orderDTO)).thenReturn(new OrderDTO(
                UUID.randomUUID(),
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
        void create_OrderDataIsInvalid_ShouldReturn400(int amount, String symbol) {
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
        void readOne_OrderExists_ShouldReturn200() {
            val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
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
    }
}

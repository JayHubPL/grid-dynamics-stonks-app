package com.griddynamics.internship.stonksjh.order.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import com.griddynamics.internship.stonksjh.order.repository.OrderRepository;

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
    OrderRepository mockedOrderRepository;

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
        }

        @ParameterizedTest(name = "{index}: orderData=[{0},{1}]")
        @MethodSource("com.griddynamics.internship.stonksjh.order.controller.util.TestDataFactory#invalidOrderData")
        @SneakyThrows
        void create_OrderDataIsInvalid_ShouldReturn400(int amount, String symbol) {
            orderDTO.setAmount(amount);
            orderDTO.setSymbol(symbol);

            mockMvc.perform(MockMvcRequestBuilders
                .post(linkTo(OrderCrudController.class.getMethod("create", OrderDTO.class), orderDTO).toUri())
                .content(new ObjectMapper().writeValueAsString(orderDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").isNotEmpty())
            .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }


    }
}

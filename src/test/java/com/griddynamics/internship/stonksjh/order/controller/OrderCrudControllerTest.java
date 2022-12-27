package com.griddynamics.internship.stonksjh.order.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
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

        @Test
        @SneakyThrows
        void create_OrderIsValid_ShouldReturn201() {
            orderDTO.setAmount(10);
            orderDTO.setSymbol("AAPL");

            val response = mockMvc.perform(MockMvcRequestBuilders
                .post(linkTo(OrderCrudController.class.getMethod("create", OrderDTO.class), orderDTO).toUri())
                .content(new ObjectMapper().writeValueAsString(orderDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isCreated())
            .andReturn()
            .getResponse();

            assertThat(response.getHeader("Location"))
                .endsWith(linkTo(OrderCrudController.class.getMethod("read", UUID.class), anyString()).toString());
        }

    }
}

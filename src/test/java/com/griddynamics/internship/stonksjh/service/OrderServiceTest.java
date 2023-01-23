package com.griddynamics.internship.stonksjh.service;

import com.griddynamics.internship.stonksjh.dto.order.OrderRequestDTO;
import com.griddynamics.internship.stonksjh.dto.order.OrderResponseDTO;
import com.griddynamics.internship.stonksjh.exception.order.InvalidOrderTypeException;
import com.griddynamics.internship.stonksjh.exception.order.InvalidStockAmountException;
import com.griddynamics.internship.stonksjh.exception.order.InvalidSymbolException;
import com.griddynamics.internship.stonksjh.exception.order.OrderNotFoundException;
import com.griddynamics.internship.stonksjh.mapper.OrderMapper;
import com.griddynamics.internship.stonksjh.model.Order;
import com.griddynamics.internship.stonksjh.repository.OrderRepository;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class OrderServiceTest {

    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);
    private final UUID VALID_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private final OrderResponseDTO PREDEFINED_ORDER = new OrderResponseDTO(VALID_UUID, Order.Type.BUY, 1, Order.Symbol.AAPL);
    @MockBean
    private OrderRepository ORDER_REPOSITORY;
    @MockBean
    private OrderMapper INJECTED_MAPPER;
    private OrderService orderService;

    @BeforeAll
    void initOrderService() {
        orderService = new OrderService(ORDER_REPOSITORY, INJECTED_MAPPER);
    }

    @Nested
    class Create {

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("util.OrderFlowTestDataFactory#validOrderData")
        void create_OrderDataIsValid_ShouldCreateOrderCorrectly(int amount, String symbol, String type) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(amount)
                    .symbol(symbol)
                    .type(type)
                    .build();

            when(INJECTED_MAPPER.requestDtoToEntity(orderRequestDTO))
                    .thenReturn(orderMapper.requestDtoToEntity(orderRequestDTO));
            when(INJECTED_MAPPER.entityToResponseDTO(any(Order.class)))
                    .thenAnswer(i -> orderMapper.entityToResponseDTO((Order) i.getArguments()[0]));
            when(ORDER_REPOSITORY.save(any(Order.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            val result = orderService.create(orderRequestDTO);
            System.err.printf("%d, %s, %s\n%n", result.amount(), result.symbol(), result.type());

            assertThat(result.amount())
                    .isEqualTo(orderRequestDTO.amount());
            assertThat(result.symbol())
                    .isEqualTo(Order.Symbol.valueOf(orderRequestDTO.symbol()));
            assertThat(result.type())
                    .isEqualTo(Order.Type.valueOf(orderRequestDTO.type()));

            verify(INJECTED_MAPPER).requestDtoToEntity(orderRequestDTO);
            verify(INJECTED_MAPPER).entityToResponseDTO(any(Order.class));
            verify(ORDER_REPOSITORY).save(any(Order.class));
        }

        @ParameterizedTest(name = "{index}: amount={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidSymbolsOrTypes")
        void create_OrderTypeIsInvalid_ShouldThrow(String type) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(1)
                    .symbol("AAPL")
                    .type(type)
                    .build();

            assertThatExceptionOfType(InvalidOrderTypeException.class)
                    .isThrownBy(() -> orderService.create(orderRequestDTO));
        }

        @ParameterizedTest(name = "{index}: amount={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidSymbolsOrTypes")
        void create_OrderSymbolIsInvalid_ShouldThrow(String symbol) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(1)
                    .symbol(symbol)
                    .type("BUY")
                    .build();

            assertThatExceptionOfType(InvalidSymbolException.class)
                    .isThrownBy(() -> orderService.create(orderRequestDTO));
        }

        @ParameterizedTest(name = "{index}: amount={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidAmounts")
        void create_OrderStockAmountIsInvalid_ShouldThrow(int amount) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(amount)
                    .symbol("AAPL")
                    .type("BUY")
                    .build();

            assertThatExceptionOfType(InvalidStockAmountException.class)
                    .isThrownBy(() -> orderService.create(orderRequestDTO));
        }

    }

    @Nested
    class Read {

        @Test
        void readOneOrder_OrderExists_ShouldReturnOrderDTO() {
            when(ORDER_REPOSITORY.findByUUID(any(UUID.class)))
                    .thenReturn(Optional.of(orderMapper.dtoToEntity(PREDEFINED_ORDER)));
            when(INJECTED_MAPPER.entityToResponseDTO(any(Order.class)))
                    .thenAnswer(i -> orderMapper.entityToResponseDTO((Order) i.getArguments()[0]));

            val result = orderService.read(PREDEFINED_ORDER.uuid());

            assertThat(result.amount())
                    .isEqualTo(PREDEFINED_ORDER.amount());
            assertThat(result.symbol())
                    .isEqualTo(PREDEFINED_ORDER.symbol());
            assertThat(result.type())
                    .isEqualTo(PREDEFINED_ORDER.type());

            verify(ORDER_REPOSITORY).findByUUID(PREDEFINED_ORDER.uuid());
            verify(INJECTED_MAPPER).entityToResponseDTO(any(Order.class));
        }

        @Test
        void readOneOrder_NoOrderWithGivenUuidExists_ShouldThrow() {
            when(ORDER_REPOSITORY.findByUUID(any(UUID.class)))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(OrderNotFoundException.class)
                    .isThrownBy(() -> orderService.read(VALID_UUID));

            verify(ORDER_REPOSITORY).findByUUID(PREDEFINED_ORDER.uuid());
        }

    }

    @Nested
    class Update {

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("util.OrderFlowTestDataFactory#validOrderData")
        void updateOrder_OrderDataIsValidAndOrderExists_ShouldUpdateOrderCorrectly(int amount, String symbol, String type) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(amount)
                    .symbol(symbol)
                    .type(type)
                    .build();

            when(INJECTED_MAPPER.entityToResponseDTO(any(Order.class)))
                    .thenAnswer(i -> orderMapper.entityToResponseDTO((Order) i.getArguments()[0]));
            when(ORDER_REPOSITORY.findByUUID(any(UUID.class)))
                    .thenReturn(Optional.of(orderMapper.dtoToEntity(PREDEFINED_ORDER)));
            when(ORDER_REPOSITORY.save(any(Order.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            val result = orderService.update(PREDEFINED_ORDER.uuid(), orderRequestDTO);

            assertThat(result.amount())
                    .isEqualTo(orderRequestDTO.amount());
            assertThat(result.symbol())
                    .isEqualTo(Order.Symbol.valueOf(orderRequestDTO.symbol()));
            assertThat(result.type())
                    .isEqualTo(Order.Type.valueOf(orderRequestDTO.type()));

            verify(INJECTED_MAPPER).entityToResponseDTO(any(Order.class));
            verify(ORDER_REPOSITORY).findByUUID(PREDEFINED_ORDER.uuid());
            verify(ORDER_REPOSITORY).save(any(Order.class));
        }

        @Test
        void updateOrder_NoOrderWithGivenUuidExists_ShouldThrow() {
            when(ORDER_REPOSITORY.findByUUID(any(UUID.class)))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(OrderNotFoundException.class)
                    .isThrownBy(
                            () -> orderService.update(PREDEFINED_ORDER.uuid(), OrderRequestDTO.builder().build())
                    );

            verify(ORDER_REPOSITORY).findByUUID(VALID_UUID);
        }

        @ParameterizedTest(name = "{index}: symbol={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidSymbolsOrTypes")
        void updateOrder_OrderSymbolIsInvalid_ShouldThrow(String symbol) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(1)
                    .symbol(symbol)
                    .type("BUY")
                    .build();

            when(ORDER_REPOSITORY.findByUUID(any(UUID.class)))
                    .thenReturn(Optional.of(orderMapper.dtoToEntity(PREDEFINED_ORDER)));

            assertThatExceptionOfType(InvalidSymbolException.class)
                    .isThrownBy(() -> orderService.update(PREDEFINED_ORDER.uuid(), orderRequestDTO));

            verify(ORDER_REPOSITORY).findByUUID(PREDEFINED_ORDER.uuid());
        }

        @ParameterizedTest(name = "{index}: type={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidSymbolsOrTypes")
        void updateOrder_OrderTypeIsInvalid_ShouldThrow(String type) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(1)
                    .symbol("AAPL")
                    .type(type)
                    .build();

            when(ORDER_REPOSITORY.findByUUID(any(UUID.class)))
                    .thenReturn(Optional.of(orderMapper.dtoToEntity(PREDEFINED_ORDER)));

            assertThatExceptionOfType(InvalidOrderTypeException.class)
                    .isThrownBy(() -> orderService.update(PREDEFINED_ORDER.uuid(), orderRequestDTO));

            verify(ORDER_REPOSITORY).findByUUID(PREDEFINED_ORDER.uuid());
        }

        @ParameterizedTest(name = "{index}: amount={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidAmounts")
        void updateOrder_OrderStockAmountIsInvalid_ShouldThrow(int amount) {
            val orderRequestDTO = OrderRequestDTO.builder()
                    .amount(amount)
                    .symbol("AAPL")
                    .type("BUY")
                    .build();

            when(ORDER_REPOSITORY.findByUUID(any(UUID.class)))
                    .thenReturn(Optional.of(orderMapper.dtoToEntity(PREDEFINED_ORDER)));

            assertThatExceptionOfType(InvalidStockAmountException.class)
                    .isThrownBy(() -> orderService.update(PREDEFINED_ORDER.uuid(), orderRequestDTO));

            verify(ORDER_REPOSITORY).findByUUID(PREDEFINED_ORDER.uuid());
        }

    }

    @Nested
    class Delete {

        @Test
        void deleteOrder_OrderExists_ShouldDeleteOrderCorrectly() {
            when(ORDER_REPOSITORY.findByUUID(any(UUID.class)))
                    .thenReturn(Optional.of(orderMapper.dtoToEntity(PREDEFINED_ORDER)));

            assertThatNoException()
                    .isThrownBy(() -> orderService.delete(PREDEFINED_ORDER.uuid()));

            verify(ORDER_REPOSITORY).findByUUID(PREDEFINED_ORDER.uuid());
        }

        @Test
        void deleteOrder_NoOrderWithGivenUuidExists_ShouldThrow() {
            when(ORDER_REPOSITORY.findByUUID(any(UUID.class)))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(OrderNotFoundException.class)
                    .isThrownBy(() -> orderService.delete(PREDEFINED_ORDER.uuid()));

            verify(ORDER_REPOSITORY).findByUUID(PREDEFINED_ORDER.uuid());
        }

    }

}

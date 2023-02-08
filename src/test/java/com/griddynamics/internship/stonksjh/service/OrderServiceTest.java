package com.griddynamics.internship.stonksjh.service;

import com.griddynamics.internship.stonksjh.dto.order.OrderCreateRequestDTO;
import com.griddynamics.internship.stonksjh.dto.order.OrderUpdateRequestDTO;
import com.griddynamics.internship.stonksjh.exception.order.InvalidOrderTypeException;
import com.griddynamics.internship.stonksjh.exception.order.InvalidStockAmountException;
import com.griddynamics.internship.stonksjh.exception.order.InvalidSymbolException;
import com.griddynamics.internship.stonksjh.exception.order.OrderNotFoundException;
import com.griddynamics.internship.stonksjh.exception.user.UserNotFoundException;
import com.griddynamics.internship.stonksjh.mapper.OrderMapper;
import com.griddynamics.internship.stonksjh.model.Order;
import com.griddynamics.internship.stonksjh.model.User;
import com.griddynamics.internship.stonksjh.repository.OrderRepository;
import com.griddynamics.internship.stonksjh.repository.UserRepository;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatList;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class OrderServiceTest {

    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);
    private final UUID ORDER_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private final UUID OWNER_UUID = UUID.fromString("a1d82886-3f7f-41dc-9cb2-d16de3d2d287");
    private User PREDEFINED_USER;
    private Order PREDEFINED_ORDER;
    @MockBean
    private OrderRepository ORDER_REPOSITORY;
    @MockBean
    private UserRepository USER_REPOSITORY;
    @MockBean
    private OrderMapper INJECTED_MAPPER;
    @MockBean
    private BrokerService BROKER_SERVICE;
    private OrderService ORDER_SERVICE;

    @BeforeAll
    void setup() {
        ORDER_SERVICE = new OrderService(
                ORDER_REPOSITORY,
                USER_REPOSITORY,
                INJECTED_MAPPER,
                BROKER_SERVICE
        );

        PREDEFINED_USER = User.builder()
                .uuid(OWNER_UUID)
                .username("user")
                .email("user@example.com")
                .build();

        PREDEFINED_ORDER = Order.builder()
                .amount(1)
                .symbol(Order.Symbol.AAPL)
                .type(Order.Type.BUY)
                .owner(PREDEFINED_USER)
                .build();
    }

    @BeforeEach
    void setupOwnerExistence() {
        when(USER_REPOSITORY.existsByUuid(OWNER_UUID))
                .thenReturn(true);
        when(USER_REPOSITORY.findByUuid(OWNER_UUID))
                .thenReturn(Optional.of(PREDEFINED_USER));
    }

    private UUID randomInvalidUUID() {
        var randomUUID = UUID.randomUUID();
        // keep re-rolling if we hit the valid one
        while (randomUUID.equals(OWNER_UUID)) {
            randomUUID = UUID.randomUUID();
        }

        return randomUUID;
    }

    @Nested
    class Create {

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("util.OrderFlowTestDataFactory#validOrderData")
        void create_OrderDataIsValid_ShouldCreateOrderCorrectly(int amount, String symbol, String type) {
            val orderRequestDTO = OrderCreateRequestDTO.builder()
                    .amount(amount)
                    .symbol(symbol)
                    .type(type)
                    .build();

            when(INJECTED_MAPPER.createRequestDtoToEntity(orderRequestDTO))
                    .thenReturn(orderMapper.createRequestDtoToEntity(orderRequestDTO));
            when(INJECTED_MAPPER.entityToResponseDTO(any(Order.class)))
                    .thenAnswer(i -> orderMapper.entityToResponseDTO((Order) i.getArguments()[0]));
            when(ORDER_REPOSITORY.save(any(Order.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            val result = ORDER_SERVICE.create(OWNER_UUID, requestBody);
            System.err.printf("%d, %s, %s\n%n", result.amount(), result.symbol(), result.type());

            assertThat(result.amount())
                    .isEqualTo(requestBody.amount());
            assertThat(result.symbol())
                    .isEqualTo(Order.Symbol.valueOf(requestBody.symbol()));
            assertThat(result.type())
                    .isEqualTo(Order.Type.valueOf(requestBody.type()));

            verify(INJECTED_MAPPER).createRequestDtoToEntity(orderRequestDTO);
            verify(INJECTED_MAPPER).entityToResponseDTO(any(Order.class));
            verify(ORDER_REPOSITORY).save(any(Order.class));
        }

        @ParameterizedTest(name = "{index}: amount={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidSymbolsOrTypes")
        void create_OrderTypeIsInvalid_ShouldThrow(String type) {
            val orderRequestDTO = OrderCreateRequestDTO.builder()
                    .amount(1)
                    .symbol("AAPL")
                    .type(type)
                    .build();

            assertThatExceptionOfType(InvalidOrderTypeException.class)
                    .isThrownBy(() -> ORDER_SERVICE.create(OWNER_UUID, requestBody));
        }

        @ParameterizedTest(name = "{index}: amount={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidSymbolsOrTypes")
        void create_OrderSymbolIsInvalid_ShouldThrow(String symbol) {
            val orderRequestDTO = OrderCreateRequestDTO.builder()
                    .amount(1)
                    .symbol(symbol)
                    .type("BUY")
                    .build();

            assertThatExceptionOfType(InvalidSymbolException.class)
                    .isThrownBy(() -> ORDER_SERVICE.create(OWNER_UUID, requestBody));
        }

        @ParameterizedTest(name = "{index}: amount={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidAmounts")
        void create_OrderStockAmountIsInvalid_ShouldThrow(int amount) {
            val orderRequestDTO = OrderCreateRequestDTO.builder()
                    .amount(amount)
                    .symbol("AAPL")
                    .type("BUY")
                    .build();

            assertThatExceptionOfType(InvalidStockAmountException.class)
                    .isThrownBy(() -> ORDER_SERVICE.create(OWNER_UUID, requestBody));
        }

        @Test
        void create_shouldThrow_whenOwnerDoesNotExist() {
            val requestBody = OrderCreateRequestDTO.builder()
                    .amount(1)
                    .symbol(Order.Symbol.AAPL.toString())
                    .type(Order.Type.BUY.toString())
                    .build();

            assertThatExceptionOfType(UserNotFoundException.class)
                    .isThrownBy(() -> ORDER_SERVICE.create(randomInvalidUUID(), requestBody));
        }

    }

    @Nested
    class Read {

        @Test
        void readOneOrder_OrderExists_ShouldReturnOrderDTO() {
            when(ORDER_REPOSITORY.findByUuidAndOwnerUuid(any(UUID.class), any(UUID.class)))
                    .thenReturn(Optional.of(PREDEFINED_ORDER));
            when(INJECTED_MAPPER.entityToResponseDTO(any(Order.class)))
                    .thenAnswer(i -> orderMapper.entityToResponseDTO((Order) i.getArguments()[0]));

            val result = ORDER_SERVICE.read(OWNER_UUID, ORDER_UUID);

            assertThat(result.amount())
                    .isEqualTo(PREDEFINED_ORDER.getAmount());
            assertThat(result.symbol())
                    .isEqualTo(PREDEFINED_ORDER.getSymbol());
            assertThat(result.type())
                    .isEqualTo(PREDEFINED_ORDER.getType());

            verify(ORDER_REPOSITORY).findByUuidAndOwnerUuid(ORDER_UUID, OWNER_UUID);
            verify(INJECTED_MAPPER).entityToResponseDTO(any(Order.class));
        }

        @Test
        void readOneOrder_NoOrderWithGivenUuidExists_ShouldThrow() {
            assertThatExceptionOfType(OrderNotFoundException.class)
                    .isThrownBy(() -> ORDER_SERVICE.read(OWNER_UUID, ORDER_UUID));
        }

        @Test
        void readOne_shouldThrow_whenOwnerDoesNotExist() {
            assertThatExceptionOfType(UserNotFoundException.class)
                    .isThrownBy(() -> ORDER_SERVICE.read(randomInvalidUUID(), ORDER_UUID));
        }

        @Test
        void readAll_shouldReturnEmptyList_whenUserHasNoOrders() {
            when(ORDER_REPOSITORY.findAllByOwnerUuid(OWNER_UUID))
                    .thenReturn(List.of());

            assertThatNoException()
                    .isThrownBy(() -> ORDER_SERVICE.read(OWNER_UUID));
            assertThat(ORDER_SERVICE.read(OWNER_UUID))
                    .isEmpty();

            verify(ORDER_REPOSITORY, times(2))
                    .findAllByOwnerUuid(OWNER_UUID);
        }

        @Test
        void readAll_shouldReturnNonEmptyList_whenUserHasOrders() {
            when(ORDER_REPOSITORY.findAllByOwnerUuid(OWNER_UUID))
                    .thenReturn(List.of(PREDEFINED_ORDER));
            when(INJECTED_MAPPER.entityToResponseDTO(PREDEFINED_ORDER))
                    .thenReturn(orderMapper.entityToResponseDTO(PREDEFINED_ORDER));

            assertThatNoException()
                    .isThrownBy(() -> ORDER_SERVICE.read(OWNER_UUID));
            val result = ORDER_SERVICE.read(OWNER_UUID);
            assertThatList(result)
                    .isNotEmpty();
            assertThatList(result)
                    .isEqualTo(List.of(orderMapper.entityToResponseDTO(PREDEFINED_ORDER)));

            verify(ORDER_REPOSITORY, times(2))
                    .findAllByOwnerUuid(OWNER_UUID);
        }

    }

    @Nested
    class Update {

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("util.OrderFlowTestDataFactory#validOrderData")
        void updateOrder_OrderDataIsValidAndOrderExists_ShouldUpdateOrderCorrectly(int amount, String symbol, String type) {
            val orderRequestDTO = OrderUpdateRequestDTO.builder()
                    .amount(amount)
                    .symbol(symbol)
                    .build();

            when(INJECTED_MAPPER.entityToResponseDTO(any(Order.class)))
                    .thenAnswer(i -> orderMapper.entityToResponseDTO((Order) i.getArguments()[0]));
            when(ORDER_REPOSITORY.findByUuidAndOwnerUuid(any(UUID.class), any(UUID.class)))
                    .thenReturn(Optional.of(PREDEFINED_ORDER));
            when(ORDER_REPOSITORY.save(any(Order.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            val result = ORDER_SERVICE.update(OWNER_UUID, ORDER_UUID, requestBody);

            assertThat(result.amount())
                    .isEqualTo(requestBody.amount());
            assertThat(result.symbol())
                    .isEqualTo(Order.Symbol.valueOf(orderRequestDTO.symbol()));

            verify(INJECTED_MAPPER).entityToResponseDTO(any(Order.class));
            verify(ORDER_REPOSITORY).findByUuidAndOwnerUuid(ORDER_UUID, OWNER_UUID);
            verify(ORDER_REPOSITORY).save(any(Order.class));
        }

        @Test
        void updateOrder_NoOrderWithGivenUuidExists_ShouldThrow() {
            val requestBody = OrderUpdateRequestDTO.builder()
                    .amount(1)
                    .symbol(Order.Symbol.AAPL.toString())
                    .build();

            when(ORDER_REPOSITORY.findByUuidAndOwnerUuid(any(UUID.class), any(UUID.class)))
                    .thenReturn(Optional.empty());

            val orderRequestDTO = OrderUpdateRequestDTO.builder()
                    .amount(1)
                    .symbol("AAPL")
                    .build();

            assertThatExceptionOfType(OrderNotFoundException.class)
                    .isThrownBy(() -> ORDER_SERVICE.update(OWNER_UUID, ORDER_UUID, orderRequestDTO));

            verify(ORDER_REPOSITORY).findByUuidAndOwnerUuid(ORDER_UUID, OWNER_UUID);
        }

        @ParameterizedTest(name = "{index}: symbol={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidSymbolsOrTypes")
        void updateOrder_OrderSymbolIsInvalid_ShouldThrow(String symbol) {
            val orderRequestDTO = OrderUpdateRequestDTO.builder()
                    .amount(1)
                    .symbol(symbol)
                    .build();

            assertThatExceptionOfType(InvalidSymbolException.class)
                    .isThrownBy(() -> ORDER_SERVICE.update(OWNER_UUID, ORDER_UUID, orderRequestDTO));
        }

        @ParameterizedTest(name = "{index}: amount={0}")
        @MethodSource("util.OrderFlowTestDataFactory#invalidAmounts")
        void updateOrder_OrderStockAmountIsInvalid_ShouldThrow(int amount) {
            val orderRequestDTO = OrderUpdateRequestDTO.builder()
                    .amount(amount)
                    .symbol("AAPL")
                    .build();

            assertThatExceptionOfType(InvalidStockAmountException.class)
                    .isThrownBy(() -> ORDER_SERVICE.update(OWNER_UUID, ORDER_UUID, orderRequestDTO));
        }

    }

    @Nested
    class Delete {

        @Test
        void deleteOrder_OrderExists_ShouldDeleteOrderCorrectly() {
            when(ORDER_REPOSITORY.findByUuidAndOwnerUuid(any(UUID.class), any(UUID.class)))
                    .thenReturn(Optional.of(PREDEFINED_ORDER));

            assertThatNoException()
                    .isThrownBy(() -> ORDER_SERVICE.delete(OWNER_UUID, ORDER_UUID));

            verify(ORDER_REPOSITORY).findByUuidAndOwnerUuid(ORDER_UUID, OWNER_UUID);
        }

        @Test
        void deleteOrder_NoOrderWithGivenUuidExists_ShouldThrow() {
            assertThatExceptionOfType(OrderNotFoundException.class)
                    .isThrownBy(() -> ORDER_SERVICE.delete(OWNER_UUID, ORDER_UUID));
        }

    }

}

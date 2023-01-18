package com.griddynamics.internship.stonksjh.order.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.griddynamics.internship.stonksjh.order.dto.CrudRequestDTO;
import com.griddynamics.internship.stonksjh.order.dto.OrderDTO;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.InvalidDataFormatException;
import com.griddynamics.internship.stonksjh.order.exception.exceptions.OrderNotFoundException;
import com.griddynamics.internship.stonksjh.order.mapper.OrderMapper;
import com.griddynamics.internship.stonksjh.order.model.Order;
import com.griddynamics.internship.stonksjh.order.model.OrderType;
import com.griddynamics.internship.stonksjh.order.model.Symbol;
import com.griddynamics.internship.stonksjh.order.repository.OrderRepository;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class OrderCrudServiceTest {
    
    @MockBean
    private OrderRepository mockedOrderRepository;
    @MockBean
    private OrderMapper mockedOrderMapper;
    @InjectMocks
    private OrderCrudService orderCrudService;

    private CrudRequestDTO crudRequestDTO;
    private OrderDTO orderDTO;
    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);
    private final UUID VALID_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private final OrderDTO PREDEFINED_ORDER = new OrderDTO(VALID_UUID, OrderType.BUY, 1, Symbol.AAPL);

    @BeforeEach
    void initCrudRequestDTO() {
        crudRequestDTO = new CrudRequestDTO();
    }

    @Nested
    class Create {

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("com.griddynamics.internship.stonksjh.util.TestDataFactory#validOrderData")
        void createOrder_OrderDataIsValid_ShouldCreateOrderCorrectly(int amount, String symbol, String orderType) {
            crudRequestDTO.setAmount(amount);
            crudRequestDTO.setSymbol(symbol);
            crudRequestDTO.setOrderType(orderType);

            when(mockedOrderMapper.requestDtoToEntity(crudRequestDTO))
                .thenReturn(orderMapper.requestDtoToEntity(crudRequestDTO));
            when(mockedOrderMapper.entityToDto(any(Order.class)))
                .thenAnswer(i -> orderMapper.entityToDto((Order)i.getArguments()[0]));
            when(mockedOrderRepository.save(any(Order.class)))
                .thenAnswer(i -> i.getArguments()[0]);

            orderDTO = orderCrudService.createOrder(crudRequestDTO);

            assertEquals(crudRequestDTO.getAmount(), orderDTO.getAmount());
            assertEquals(Symbol.valueOf(crudRequestDTO.getSymbol()), orderDTO.getSymbol());
            assertEquals(OrderType.valueOf(crudRequestDTO.getOrderType()), orderDTO.getOrderType());

            verify(mockedOrderMapper).requestDtoToEntity(crudRequestDTO);
            verify(mockedOrderMapper).entityToDto(any(Order.class));
            verify(mockedOrderRepository).save(any(Order.class));
        }

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("com.griddynamics.internship.stonksjh.util.TestDataFactory#invalidOrderData")
        void createOrder_OrderDataIsInvalid_ShouldThrow(int amount, String symbol, String orderType) {
            crudRequestDTO.setAmount(amount);
            crudRequestDTO.setSymbol(symbol);
            crudRequestDTO.setOrderType(orderType);

            assertThrows(InvalidDataFormatException.class, () -> {
                orderCrudService.createOrder(crudRequestDTO);
            });
        }

    }

    @Nested
    class Read {

        @Test
        void readOneOrder_OrderExists_ShouldReturnOrderDTO() {           
            when(mockedOrderRepository.findByUUID(any(UUID.class)))
                .thenReturn(Optional.of(orderMapper.dtoToEntity(PREDEFINED_ORDER)));
            when(mockedOrderMapper.entityToDto(any(Order.class)))
                .thenAnswer(i -> orderMapper.entityToDto((Order)i.getArguments()[0]));

            orderDTO = orderCrudService.readOneOrder(PREDEFINED_ORDER.getUuid());

            assertEquals(PREDEFINED_ORDER.getAmount(), orderDTO.getAmount());
            assertEquals(PREDEFINED_ORDER.getSymbol(), orderDTO.getSymbol());
            assertEquals(PREDEFINED_ORDER.getOrderType(), orderDTO.getOrderType());
            
            verify(mockedOrderRepository).findByUUID(PREDEFINED_ORDER.getUuid());
            verify(mockedOrderMapper).entityToDto(any(Order.class));
        }

        @Test
        void readOneOrder_NoOrderWithGivenUuidExists_ShouldThrow() {
            when(mockedOrderRepository.findByUUID(any(UUID.class)))
                .thenReturn(Optional.empty());

            assertThrows(OrderNotFoundException.class, () -> {
                orderCrudService.readOneOrder(VALID_UUID);
            });

            verify(mockedOrderRepository).findByUUID(PREDEFINED_ORDER.getUuid());
        }

    }

    @Nested
    class Update {

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("com.griddynamics.internship.stonksjh.util.TestDataFactory#validOrderData")
        void updateOrder_OrderDataIsValidAndOrderExists_ShouldUpdateOrderCorrectly(int amount, String symbol, String orderType) {
            crudRequestDTO.setAmount(amount);
            crudRequestDTO.setSymbol(symbol);
            crudRequestDTO.setOrderType(orderType);

            when(mockedOrderMapper.entityToDto(any(Order.class)))
                .thenAnswer(i -> orderMapper.entityToDto((Order)i.getArguments()[0]));
            when(mockedOrderRepository.findByUUID(any(UUID.class)))
                .thenReturn(Optional.of(orderMapper.dtoToEntity(PREDEFINED_ORDER)));
            when(mockedOrderRepository.save(any(Order.class)))
                .thenAnswer(i -> i.getArguments()[0]);

            orderDTO = orderCrudService.updateOrder(PREDEFINED_ORDER.getUuid(), crudRequestDTO);

            assertEquals(crudRequestDTO.getAmount(), orderDTO.getAmount());
            assertEquals(Symbol.valueOf(crudRequestDTO.getSymbol()), orderDTO.getSymbol());
            assertEquals(OrderType.valueOf(crudRequestDTO.getOrderType()), orderDTO.getOrderType());

            verify(mockedOrderMapper).entityToDto(any(Order.class));
            verify(mockedOrderRepository).findByUUID(PREDEFINED_ORDER.getUuid());
            verify(mockedOrderRepository).save(any(Order.class));
        }

        @Test
        void updateOrder_NoOrderWithGivenUuidExists_ShouldThrow() {
            when(mockedOrderRepository.findByUUID(any(UUID.class)))
                .thenReturn(Optional.empty());

            assertThrows(OrderNotFoundException.class, () -> {
                orderCrudService.updateOrder(PREDEFINED_ORDER.getUuid(), new CrudRequestDTO());
            });

            verify(mockedOrderRepository).findByUUID(VALID_UUID);
        }

        @ParameterizedTest(name = "{index}: orderData=[{0},{1},{2}]")
        @MethodSource("com.griddynamics.internship.stonksjh.util.TestDataFactory#invalidOrderData")
        void updateOrder_OrderDataIsInvalid_ShouldThrow(int amount, String symbol, String orderType) {
            crudRequestDTO.setAmount(amount);
            crudRequestDTO.setSymbol(symbol);
            crudRequestDTO.setOrderType(orderType);

            when(mockedOrderRepository.findByUUID(any(UUID.class)))
                .thenReturn(Optional.of(orderMapper.dtoToEntity(PREDEFINED_ORDER)));

            assertThrows(InvalidDataFormatException.class, () -> {
                orderCrudService.updateOrder(PREDEFINED_ORDER.getUuid(), new CrudRequestDTO());
            });

            verify(mockedOrderRepository).findByUUID(PREDEFINED_ORDER.getUuid());
        }

    }

    @Nested
    class Delete {

        @Test
        void deleteOrder_OrderExists_ShouldDeleteOrderCorrectly() {
            when(mockedOrderRepository.findByUUID(any(UUID.class)))
                .thenReturn(Optional.of(orderMapper.dtoToEntity(PREDEFINED_ORDER)));

            assertDoesNotThrow(() -> orderCrudService.deleteOrder(PREDEFINED_ORDER.getUuid()));

            verify(mockedOrderRepository).findByUUID(PREDEFINED_ORDER.getUuid());
        }

        @Test
        void deleteOrder_NoOrderWithGivenUuidExists_ShouldThrow() {
            when(mockedOrderRepository.findByUUID(any(UUID.class)))
                .thenReturn(Optional.empty());

            assertThrows(OrderNotFoundException.class, () -> {
                orderCrudService.deleteOrder(PREDEFINED_ORDER.getUuid());
            });

            verify(mockedOrderRepository).findByUUID(PREDEFINED_ORDER.getUuid());
        }

    }

}

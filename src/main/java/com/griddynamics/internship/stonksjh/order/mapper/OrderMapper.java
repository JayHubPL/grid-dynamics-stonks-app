package com.griddynamics.internship.stonksjh.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.griddynamics.internship.stonksjh.order.dto.OrderDTO;
import com.griddynamics.internship.stonksjh.order.model.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "orderType", source = "orderType")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "symbol", source = "symbol")
    OrderDTO entityToDto(Order entity);

    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "orderType", source = "orderType")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "symbol", source = "symbol")
    Order dtoToEntity(OrderDTO dto);

}

package com.griddynamics.internship.stonksjh.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.griddynamics.internship.stonksjh.order.dto.OrderDTO;
import com.griddynamics.internship.stonksjh.order.model.Order;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "orderType", source = "orderType")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "symbol", source = "symbol")
    OrderDTO entityToDto(Order entity); // TODO Unmapped target property: "add" (what is this?)

    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "orderType", source = "orderType")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "symbol", source = "symbol")
    Order dtoToEntity(OrderDTO dto);

}

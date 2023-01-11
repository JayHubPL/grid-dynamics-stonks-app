package com.griddynamics.internship.stonksjh.order.mapper;

import org.mapstruct.Mapper;

import com.griddynamics.internship.stonksjh.order.dto.OrderDTO;
import com.griddynamics.internship.stonksjh.order.model.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderDTO entityToDto(Order entity);

    Order dtoToEntity(OrderDTO dto);

}

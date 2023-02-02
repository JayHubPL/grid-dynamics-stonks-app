package com.griddynamics.internship.stonksjh.mapper;

import com.griddynamics.internship.stonksjh.dto.order.OrderCreateRequestDTO;
import com.griddynamics.internship.stonksjh.dto.order.OrderResponseDTO;
import com.griddynamics.internship.stonksjh.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponseDTO entityToResponseDTO(Order entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "status", ignore = true)
    Order createRequestDtoToEntity(OrderCreateRequestDTO orderCreateRequestDTO);

}

package com.griddynamics.internship.stonksjh.user.mapper;

import com.griddynamics.internship.stonksjh.user.dto.CrudRequestDTO;
import com.griddynamics.internship.stonksjh.user.dto.UserDTO;
import com.griddynamics.internship.stonksjh.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO entityToDto(User user);

    User dtoToEntity(UserDTO userDTO);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "balance", ignore = true)
    User requestDtoToEntity(CrudRequestDTO crudRequestDTO);

}

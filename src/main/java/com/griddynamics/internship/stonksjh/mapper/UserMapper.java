package com.griddynamics.internship.stonksjh.mapper;

import com.griddynamics.internship.stonksjh.dto.user.UserRequestDTO;
import com.griddynamics.internship.stonksjh.dto.user.UserResponseDTO;
import com.griddynamics.internship.stonksjh.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO entityToResponseDTO(User user);

    User dtoToEntity(UserResponseDTO userResponseDTO);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "balance", ignore = true)
    User requestDtoToEntity(UserRequestDTO userRequestDTO);

}

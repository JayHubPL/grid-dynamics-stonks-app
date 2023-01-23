package com.griddynamics.internship.stonksjh.service;

import com.griddynamics.internship.stonksjh.dto.user.UserRequestDTO;
import com.griddynamics.internship.stonksjh.dto.user.UserResponseDTO;
import com.griddynamics.internship.stonksjh.exception.user.EmailFormatException;
import com.griddynamics.internship.stonksjh.exception.user.EmailTakenException;
import com.griddynamics.internship.stonksjh.exception.user.UserNotFoundException;
import com.griddynamics.internship.stonksjh.exception.user.UsernameFormatException;
import com.griddynamics.internship.stonksjh.exception.user.UsernameTakenException;
import com.griddynamics.internship.stonksjh.mapper.UserMapper;
import com.griddynamics.internship.stonksjh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    public UUID create(UserRequestDTO userRequestDTO) {
        validateEmail(userRequestDTO.email());
        validateUsername(userRequestDTO.username());

        if (repository.findByEmail(userRequestDTO.email()).isPresent()) {
            throw new EmailTakenException(userRequestDTO.email());
        }
        if (repository.findByUsername(userRequestDTO.username()).isPresent()) {
            throw new UsernameTakenException(userRequestDTO.username());
        }

        val entity = mapper.requestDtoToEntity(userRequestDTO);
        val uuid = UUID.randomUUID();
        entity.setUuid(uuid);
        repository.save(entity);

        return uuid;
    }

    public UserResponseDTO read(UUID uuid) {
        val user = repository.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException(uuid));

        return mapper.entityToResponseDTO(user);
    }

    public List<UserResponseDTO> read() {
        return repository.findAll().stream()
                .map(mapper::entityToResponseDTO)
                .toList();
    }

    public UserResponseDTO update(UUID uuid, UserRequestDTO userRequestDTO) {
        val updated = repository.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException(uuid));

        validateEmail(userRequestDTO.email());
        validateUsername(userRequestDTO.username());

        val emailOpt = repository.findByEmail(userRequestDTO.email());
        val usernameOpt = repository.findByUsername(userRequestDTO.username());
        if (emailOpt.isPresent() && !emailOpt.get().getUuid().equals(uuid)) {
            throw new EmailTakenException(userRequestDTO.email());
        }
        if (usernameOpt.isPresent() && !usernameOpt.get().getUuid().equals(uuid)) {
            throw new UsernameTakenException(userRequestDTO.username());
        }

        updated.setEmail(userRequestDTO.email());
        updated.setUsername(userRequestDTO.username());

        return mapper.entityToResponseDTO(repository.save(updated));
    }

    public void delete(UUID uuid) {
        val deleted = repository.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException(uuid));

        repository.delete(deleted);
    }

    private void validateEmail(String email) {
        val regex = "\\w+@\\w+\\.\\w+";

        if (!email.matches(regex)) {
            throw new EmailFormatException(email);
        }
    }

    private void validateUsername(String username) {
        val regex = "\\w+";

        if (!username.matches(regex)) {
            throw new UsernameFormatException(username);
        }
    }

}

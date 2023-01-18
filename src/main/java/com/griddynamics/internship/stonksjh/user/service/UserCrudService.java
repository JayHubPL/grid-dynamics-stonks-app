package com.griddynamics.internship.stonksjh.user.service;

import com.griddynamics.internship.stonksjh.user.dto.CrudRequestDTO;
import com.griddynamics.internship.stonksjh.user.dto.UserDTO;
import com.griddynamics.internship.stonksjh.user.exception.exceptions.EmailFormatException;
import com.griddynamics.internship.stonksjh.user.exception.exceptions.UserEmailTakenException;
import com.griddynamics.internship.stonksjh.user.exception.exceptions.UserNotFoundException;
import com.griddynamics.internship.stonksjh.user.exception.exceptions.UserUsernameTakenException;
import com.griddynamics.internship.stonksjh.user.exception.exceptions.UsernameFormatException;
import com.griddynamics.internship.stonksjh.user.mapper.UserMapper;
import com.griddynamics.internship.stonksjh.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserCrudService {

    private final UserRepository repository;
    private final UserMapper mapper;

    public UUID create(CrudRequestDTO crudRequestDTO) {
        validateEmail(crudRequestDTO.getEmail());
        validateUsername(crudRequestDTO.getUsername());

        if (repository.findByEmail(crudRequestDTO.getEmail()).isPresent()) {
            throw new UserEmailTakenException(crudRequestDTO.getEmail());
        }
        if (repository.findByUsername(crudRequestDTO.getUsername()).isPresent()) {
            throw new UserUsernameTakenException(crudRequestDTO.getUsername());
        }

        val entity = mapper.requestDtoToEntity(crudRequestDTO);
        val uuid = UUID.randomUUID();
        entity.setUuid(uuid);
        repository.save(entity);

        return uuid;
    }

    public UserDTO readOne(UUID uuid) {
        val user = repository.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException(uuid));

        return mapper.entityToDto(user);
    }

    public List<UserDTO> readAll() {
        return repository.findAll().stream()
                .map(mapper::entityToDto)
                .toList();
    }

    public UserDTO update(UUID uuid, CrudRequestDTO crudRequestDTO) {
        val updated = repository.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException(uuid));

        validateEmail(crudRequestDTO.getEmail());
        validateUsername(crudRequestDTO.getUsername());

        val emailOpt = repository.findByEmail(crudRequestDTO.getEmail());
        val usernameOpt = repository.findByUsername(crudRequestDTO.getUsername());
        if (emailOpt.isPresent() && !emailOpt.get().getUuid().equals(uuid)) {
            throw new UserEmailTakenException(crudRequestDTO.getEmail());
        }
        if (usernameOpt.isPresent() && !usernameOpt.get().getUuid().equals(uuid)) {
            throw new UserUsernameTakenException(crudRequestDTO.getUsername());
        }

        updated.setEmail(crudRequestDTO.getEmail());
        updated.setUsername(crudRequestDTO.getUsername());

        return mapper.entityToDto(repository.save(updated));
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

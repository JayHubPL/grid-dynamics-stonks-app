package com.griddynamics.internship.stonksjh.controller;

import com.griddynamics.internship.stonksjh.dto.user.UserRequestDTO;
import com.griddynamics.internship.stonksjh.dto.user.UserResponseDTO;
import com.griddynamics.internship.stonksjh.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService crudService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UUID> create(@RequestBody UserRequestDTO userRequestDTO) {
        val uuid = crudService.create(userRequestDTO);

        return new ResponseEntity<>(uuid, HttpStatus.CREATED);
    }

    @GetMapping(
            value = "/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserResponseDTO> read(@PathVariable UUID uuid) {
        return ResponseEntity.ok(crudService.read(uuid));
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<UserResponseDTO>> read() {
        return ResponseEntity.ok(crudService.read());
    }

    @PutMapping(
            value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserResponseDTO> update(@PathVariable UUID uuid, @RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.ok(crudService.update(uuid, userRequestDTO));
    }

    @DeleteMapping(
            value = "/{uuid}"
    )
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        crudService.delete(uuid);

        return ResponseEntity.ok(null);
    }

}

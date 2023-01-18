package com.griddynamics.internship.stonksjh.user.controller;

import com.griddynamics.internship.stonksjh.user.dto.CrudRequestDTO;
import com.griddynamics.internship.stonksjh.user.dto.UserDTO;
import com.griddynamics.internship.stonksjh.user.service.UserCrudService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
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


/**
 * RESTful api for CRUD operations regarding users of our application
 *
 * @author jlaba
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserCrudController {

    private final UserCrudService crudService;

    /**
     * Creates a new user
     *
     * @param crudRequestDTO user's data
     * @return {@link ResponseEntity}:
     * <p>HTTP 201 CREATED on success</p>
     * <p>HTTP 409 CONFLICT on attempt to create user with taken email or username</p>
     */
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UUID> create(@RequestBody CrudRequestDTO crudRequestDTO) {
        val uuid = crudService.create(crudRequestDTO);

        return new ResponseEntity<>(uuid, HttpStatus.CREATED);
    }

    /**
     * Fetches one chosen user
     *
     * @param uuid unique identifier of the user whose information we want to fetch
     * @return {@link ResponseEntity}:
     * <p>HTTP 200 OK + {@link UserDTO} with desired data on success</p>
     * <p>HTTP 404 NOT FOUND on attempt to fetch non-existent user</p>
     */
    @GetMapping(
            value = "/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDTO> readOne(@PathVariable UUID uuid) {
        return ResponseEntity.ok(crudService.readOne(uuid));
    }

    /**
     * Fetches all existing users
     *
     * @return {@link ResponseEntity}:
     * <p>HTTP 200 OK + {@link List} of users' data (can be empty)</p>
     */
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<UserDTO>> readAll() {
        return ResponseEntity.ok(crudService.readAll());
    }

    /**
     * Updates chosen user's information
     *
     * @param uuid           unique identifier of user whose information we want to update
     * @param crudRequestDTO updated user's data
     * @return {@link ResponseEntity}:
     * <p>HTTP 200 OK + {@link UserDTO} with updated data</p>
     * <p>HTTP 400 BAD REQUEST when email or username has invalid formatting</p>
     * <p>HTTP 404 NOT FOUND on attempt to update non-existent user</p>
     * <p>HTTP 409 CONFLICT on attempt to update the user with already taken username or email</p>
     */
    @PutMapping(
            value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDTO> update(@PathVariable UUID uuid, @RequestBody CrudRequestDTO crudRequestDTO) {
        return ResponseEntity.ok(crudService.update(uuid, crudRequestDTO));
    }

    /**
     * Deletes chosen user
     *
     * @param uuid unique identifier of the user we want to delete
     * @return {@link ResponseEntity}
     * <p>HTTP 200 OK + {@link UserDTO} of deleted user on success</p>
     * <p>HTTP 404 NOT FOUND on attempt to delete non-existent user</p>
     */
    @DeleteMapping(
            value = "/{uuid}"
    )
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        crudService.delete(uuid);

        return ResponseEntity.ok(null);
    }

}

package com.griddynamics.internship.stonksjh.user.repository;

import com.griddynamics.internship.stonksjh.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUuid(UUID uuid);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

}

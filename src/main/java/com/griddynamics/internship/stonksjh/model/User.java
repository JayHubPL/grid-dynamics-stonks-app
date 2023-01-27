package com.griddynamics.internship.stonksjh.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "usr") // "user" is a reserved keyword in PostgreSQL
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(
            nullable = false,
            unique = true
    )
    private UUID uuid;

    @Column(
            nullable = false,
            unique = true
    )
    private String email;

    @Column(
            nullable = false,
            unique = true
    )
    private String username;

    @Column(
            nullable = false,
            columnDefinition = "Decimal(10,2) default '0.00'"
    )
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

}

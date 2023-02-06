package com.griddynamics.internship.stonksjh.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;
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

    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyEnumerated(EnumType.STRING)
    private Map<Order.Symbol, Integer> stocks;

}

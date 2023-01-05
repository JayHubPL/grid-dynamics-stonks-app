package com.griddynamics.internship.stonksjh.order.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "order")
@NoArgsConstructor
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(
        strategy = IDENTITY
    )
    @Column(
        updatable = false
    )
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(
        updatable = false,
        nullable = false,
        unique = true
    )
    private UUID uuid;

    @Column(
        nullable = false
    )
    private OrderType orderType;

    @Column(
        nullable = false
    )
    private int amount;

    @Column(
        nullable = false
    )
    private String symbol;

}

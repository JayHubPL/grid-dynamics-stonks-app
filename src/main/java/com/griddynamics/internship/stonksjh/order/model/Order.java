package com.griddynamics.internship.stonksjh.order.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "Order")
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
        name = "id",
        updatable = false
    )
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(
        name = "uuid",
        updatable = false,
        nullable = false,
        unique = true
    )
    private UUID uuid;

    @Column(
        name = "orderType",
        nullable = false
    )
    private OrderType orderType;

    @Column(
        name = "amount",
        nullable = false
    )
    private int amount;

    @Column(
        name = "symbol",
        nullable = false
    )
    private String symbol;

}

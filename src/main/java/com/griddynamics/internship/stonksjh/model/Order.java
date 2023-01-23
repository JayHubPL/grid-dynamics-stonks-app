package com.griddynamics.internship.stonksjh.model;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import static jakarta.persistence.GenerationType.IDENTITY;

@Table(name = "order")
@NoArgsConstructor
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(updatable = false)
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(
            updatable = false,
            nullable = false,
            unique = true
    )
    private UUID uuid;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Type type;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Symbol symbol;

    public enum Type {
        BUY,
        SELL
    }

    public enum Symbol {
        AAPL,
        META,
        NVDA,
        AMZN,
        GOOG,
        TSLA,
        MSFT,
        JNJ
    }

}

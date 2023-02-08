package com.griddynamics.internship.stonksjh.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private BigDecimal bid;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(
            nullable = false,
            updatable = false
    )
    private User owner;

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

    public enum Status {
        PENDING,
        COMPLETE
    }

}

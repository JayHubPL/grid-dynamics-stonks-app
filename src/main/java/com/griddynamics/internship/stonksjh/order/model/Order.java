package com.griddynamics.internship.stonksjh.order.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity(name = "Order")
@AllArgsConstructor
@RequiredArgsConstructor
public class Order {

    @Id
    @SequenceGenerator(
        name = "order_sequence",
        sequenceName = "order_sequence",
        allocationSize = 1
    )
    @GeneratedValue(
        strategy = IDENTITY,
        generator = "order_sequence"
    )
    @Column(
        name = "id",
        updatable = false
    )
    @Getter
    private long id;

    @Column(
        name = "orderType",
        nullable = false
    )
    @NonNull
    @Getter 
    @Setter
    private OrderType orderType;

    public enum OrderType {
        BUY,
        SELL;
    }

}

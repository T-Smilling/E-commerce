package com.javaweb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Email
    @Column(nullable = false, name = "email")
    private String email;

    @OneToMany(mappedBy = "order", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    @Column(name = "order_date")
    private LocalDate orderDate;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private PaymentEntity payment;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "order_status")
    private String orderStatus;
}

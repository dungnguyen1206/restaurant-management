package com.rroms.restaurantmanagement.entity;

import com.rroms.restaurantmanagement.entity.constant.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "invoice_id")

    private Long invoiceId;

    private BigDecimal total;

    private BigDecimal deposit;

    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    private Instant paidAt;

    private Instant createdAt;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
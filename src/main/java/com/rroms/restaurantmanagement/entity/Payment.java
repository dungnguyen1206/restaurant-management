package com.rroms.restaurantmanagement.entity;

import com.rroms.restaurantmanagement.entity.constant.PaymentMethod;
import com.rroms.restaurantmanagement.entity.constant.PaymentStatus;
import com.rroms.restaurantmanagement.entity.constant.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, columnDefinition = "varchar(255) default 'CASH'")
    private PaymentMethod paymentMethod;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'PENDING'")
    private PaymentStatus status;

    @Column(name = "paid_at", nullable = false, columnDefinition = "datetime2(6) default sysdatetime()")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime2(6) default sysdatetime()")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = true)
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, columnDefinition = "varchar(255) default 'FINAL_PAYMENT'")
    private PaymentType paymentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (paidAt == null) {
            paidAt = now;
        }
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
        if (paymentMethod == null) {
            paymentMethod = PaymentMethod.CASH;
        }
        if (paymentType == null) {
            paymentType = PaymentType.FINAL_PAYMENT;
        }
    }
}

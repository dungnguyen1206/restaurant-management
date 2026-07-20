package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}

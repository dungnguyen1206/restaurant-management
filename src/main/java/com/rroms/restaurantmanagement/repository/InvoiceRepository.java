package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}

package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("SELECT sum(i.finalAmount) from Invoice i where month (i.paidAt)=:month and year(i.paidAt)=:year")
    BigDecimal findByInvoiceNumber(String invoiceNumber, int month,int year);

    List<Invoice> findByStatus(String status);

    List<Invoice> findByCustomerId(Long customerId);





}

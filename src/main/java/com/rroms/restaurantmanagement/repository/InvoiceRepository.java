package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.dto.response.revenue.RevenueSummaryCard;
import com.rroms.restaurantmanagement.dto.response.revenue.RevenueSummaryProjection;
import com.rroms.restaurantmanagement.dto.response.revenue.RevenueTrendProjection;
import com.rroms.restaurantmanagement.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("Select new com.rroms.restaurantmanagement.dto.response.revenue.RevenueSummaryCard(" +
            " sum(i.total), " +
            " count(i.invoiceId) ) " +
            " from Invoice i where i.paidAt between :startDate and :endDate")
    RevenueSummaryCard getSummaryTotalByPeriod(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);






    @Query(" select i from Invoice i left join fetch i.payments p left join fetch i.order d where i.paidAt >= :startDate and i.paidAt < :endDate")
    Page<Invoice> getInvoicesByPeriod(@Param("startDate") Instant startDate,
                                      @Param("endDate") Instant endDate, Pageable pageable);

    @Query(value = " select day(i.paidAt) as dayNumber, " +
            " month(i.paidAt) as monthNumber, " +
            " year(i.paidAt) as yearNumber, " +
            " coalesce(sum(i.total), 0) as revenue " +
            " from invoices i " +
            " where i.paidAt >= :startDate and i.paidAt < :endDate " +
            " group by year(i.paidAt), month(i.paidAt), day(i.paidAt) " +
            " order by year(i.paidAt), month(i.paidAt), day(i.paidAt)", nativeQuery = true)
    List<RevenueTrendProjection> getRevenueTrendByDay(@Param("startDate") Instant startDate,
                                                       @Param("endDate") Instant endDate);

    @Query(value = " select month(i.paidAt) as monthNumber, " +
            " year(i.paidAt) as yearNumber, " +
            " coalesce(sum(i.total), 0) as revenue " +
            " from invoices i " +
            " where i.paidAt >= :startDate and i.paidAt < :endDate " +
            " group by year(i.paidAt), month(i.paidAt) " +
            " order by year(i.paidAt), month(i.paidAt)", nativeQuery = true)
    List<RevenueTrendProjection> getRevenueTrendByMonth(@Param("startDate") Instant startDate,
                                                         @Param("endDate") Instant endDate);

    @Query(value = " select year(i.paidAt) as yearNumber, " +
            " coalesce(sum(i.total), 0) as revenue " +
            " from invoices i " +
            " where i.paidAt >= :startDate and i.paidAt < :endDate " +
            " group by year(i.paidAt) " +
            " order by year(i.paidAt)", nativeQuery = true)
    List<RevenueTrendProjection> getRevenueTrendByYear(@Param("startDate") Instant startDate,
                                                        @Param("endDate") Instant endDate);


}

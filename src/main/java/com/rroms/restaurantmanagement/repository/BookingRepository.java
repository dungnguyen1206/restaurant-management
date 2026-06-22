package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 1. Lấy tất cả reservation của customer
    List<Reservation> findByCustomerId(Long customerId);

    // 2. Lấy reservation theo status
    List<Reservation> findByStatus(String status);

    // 3. Lấy reservation theo customer + status
    List<Reservation> findByCustomerIdAndStatus(Long customerId, String status);

    // 4. Lấy reservation trong khoảng thời gian
    List<Reservation> findByBookingDateBetween(LocalDateTime start, LocalDateTime end);

    // 5. Check trùng booking (room + time range)
    boolean existsByRoomIdAndBookingDateBetween(
            Long roomId,
            LocalDateTime start,
            LocalDateTime end
    );

    // 6. Lấy booking mới nhất của customer
    Optional<Reservation> findFirstByCustomerIdOrderByBookingDateDesc(Long customerId);

    // 7. Đếm booking theo room
    long countByRoomId(Long roomId);

    // 8. Lấy reservation bị timeout (PENDING quá hạn)
    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.status = 'PENDING'
        AND r.createdAt < :timeout
    """)
    List<Reservation> findExpiredReservations(@Param("timeout") LocalDateTime timeout);

    // 9. Thống kê doanh thu theo tháng (native query)
    @Query(value = """
        SELECT COALESCE(SUM(total_price), 0)
        FROM reservations
        WHERE status = 'COMPLETED'
        AND MONTH(booking_date) = :month
    """, nativeQuery = true)
    Double calculateMonthlyRevenue(@Param("month") int month);
}
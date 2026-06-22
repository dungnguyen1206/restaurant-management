package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Reservation, Long> {

    // 1. Tìm tất cả booking của một khách hàng
    List<Reservation> findByCustomerId(Long customerId);

    // 2. Tìm các booking theo trạng thái (e.g., PENDING, CONFIRMED, CANCELLED)
    List<Reservation> findByStatus(String status);

    // 3. Tìm booking của khách hàng theo trạng thái cụ thể
    List<Reservation> findByCustomerIdAndStatus(Long customerId, String status);

    //Khánh CHÓ
    // 4. Tìm các booking trong khoảng thời gian (Tìm lịch trùng, báo cáo doanh thu...)
    List<Reservation> findByBookingDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 5. Kiểm tra xem một phòng/bàn/xe đã bị đặt trong khoảng thời gian này chưa (Tránh đặt trùng)
    boolean existsByRoomIdAndBookingDateBetween(Long roomId, LocalDateTime start, LocalDateTime end);

    // 6. Tìm booking mới nhất của một khách hàng
    Optional<Reservation> findFirstByCustomerIdOrderByBookingDateDesc(Long customerId);

    // 7. Đếm tổng số booking của một phòng
    long countByRoomId(Long roomId);

    // 8. Viết Custom Query (JPQL) - Lấy danh sách booking quá hạn chưa thanh toán
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.createdAt < :timeout")
    List<Reservation> findExpiredBookings(@Param("timeout") LocalDateTime timeout);

    // 9. Viết Native Query (SQL thuần) - Thống kê doanh thu theo tháng (Ví dụ vui vẻ)
    @Query(value = "SELECT SUM(total_price) FROM bookings WHERE status = 'COMPLETED' AND MONTH(booking_date) = :month", nativeQuery = true)
    Double calculateMonthlyRevenue(@Param("month") int month);

    @Modifying
    @Transactional
    @Query("UPDATE Booking b SET b.status = :status WHERE b.id IN :ids")
    int updateStatusForIds(@Param("status") String status, @Param("ids") List<Long> ids);

    // 5. Tối ưu hiệu năng: Dùng EntityGraph để fetch luôn thông tin Customer và Room (Tránh lỗi N+1 Query)
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"customer", "room"})
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Reservation> findByIdWithDetails(@Param("id") Long id);


    // 7. Tìm các booking có ngày checkout nằm trong quá khứ nhưng trạng thái vẫn là 'CHECKED_IN' (Để nhắc nhở)
    List<Reservation> findByCheckOutTimeBeforeAndStatus(LocalDateTime now, String status);

    // 8. Tìm kiếm kết hợp nhiều điều kiện (Dynamic-like query bằng JPQL)
    @Query("SELECT b FROM Booking b WHERE (:status IS NULL OR b.status = :status) " +
            "AND (:customerId IS NULL OR b.customer.id = :customerId)")
    List<Reservation> findByFilter(@Param("status") String status, @Param("customerId") Long customerId);
}
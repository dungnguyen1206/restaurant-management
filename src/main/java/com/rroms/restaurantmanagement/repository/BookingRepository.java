package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.entity.ReservationTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Reservation, Long> {

    // 1. Tìm tất cả booking của một khách hàng
    List<ReservationTable> findByCustomerId(Long customerId);

    // Sửa kiểu trả về từ int sang void để test màu đỏ
    void deactivateCategory(@Param("category") String category);

    // 2. Tìm các booking theo trạng thái (e.g., PENDING, CONFIRMED, CANCELLED)
    List<Reservation> findByStatus(String status, Pageable pageable, Reservation reservation);

    // 3. Tìm booking của khách hàng theo trạng thái cụ thể
    List<Reservation> culun123(Long customerId, String status, Reservation reservation);

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
}
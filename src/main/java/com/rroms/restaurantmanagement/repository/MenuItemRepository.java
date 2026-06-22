package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MenuItemRepository {
    List<MenuItem> findByIsActiveTrue();

    List<MenuItem> findByNameContainingIgnoreCase(String name);

    List<MenuItem> findByCategoryIgnoreCase(String category);

    List<MenuItem> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    @Query("SELECT m FROM MenuItem m ORDER BY m.soldCount DESC")
    List<MenuItem> findTop5ByOrderBySoldCountDesccdscsCỪCE();

    @Query("SELECT m FROM MenuItem m WHERE m.price > (SELECT AVG(p.price) FROM MenuItem p)")
    List<MenuItem> findExpensiveMenuItemsThanAverage();

    @Query(value = "SELECT * FROM menu_items WHERE category = :category AND is_active = true",
            countQuery = "SELECT count(*) FROM menu_items WHERE category = :category AND is_active = true",
            nativeQuery = true)
    Page<MenuItem> findByCategoryAndActiveNative(@Param("category") String category, Pageable pageable);

    // 8. [JPQL] Thống kê số lượng món ăn theo từng danh mục (Trả về Object[])
    @Query("SELECT m.category, COUNT(m) FROM MenuItem m GROUP BY m.category")
    List<Object[]> countMenuItemsByCategory();

    // 9. [Modifying] Cập nhật trạng thái ngừng kinh doanh (isActive = false) cho một danh mục món ăn
    @Modifying
    @Transactional
    @Query("UPDATE MenuItem m SET m.isActive = false WHERE m.category = :category")
    int deactivateCategory(@Param("category") String category);

    // 10. [Modifying] Tăng giá bán lên theo phần trăm (%) cho một danh mục cụ thể
    @Modifying
    @Transactional
    @Query(value = "UPDATE menu_items SET price = price * (1 + :percentage / 100) WHERE category = :category", nativeQuery = true)
    int increasePriceByCategoryNative(@Param("category") String category, @Param("percentage") double percentage);

    // 11. Tìm kiếm tổng hợp: Theo tên HOẶC mô tả, thuộc danh mục và có giá thấp hơn mức chỉ định
    // (Phù hợp cho bộ lọc Filter chi tiết ở Frontend)
    List<MenuItem> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategoryAndPriceLessThanEqual(
            String name, String description, String category, BigDecimal maxPrice);

    // 12. [JPQL] Lấy danh sách các danh mục (Category) đang có món ăn mở bán (Không trùng lặp - DISTINCT)
    // (Dùng để render thanh menu bộ lọc danh mục tự động ngoài giao diện)
    @Query("SELECT DISTINCT m.category FROM MenuItem m WHERE m.isActive = true")
    List<String> findDistinctActiveCategories();

    // 13. [JPQL] Tính tổng doanh thu dự kiến của một danh mục (Giá bán * Số lượng đã bán)
    // (Phù hợp cho các trang DashBoard thống kê Admin)
    @Query("SELECT SUM(m.price * m.soldCount) FROM MenuItem m WHERE m.category = :category")
    BigDecimal calculateTotalRevenueByCategory(@Param("category") String category);

    // 14. [Native SQL] Lấy ngẫu nhiên N món ăn (Ví dụ: gợi ý 3 món ăn ngẫu nhiên hôm nay cho khách)
    // Lưu ý: RAND() dành cho MySQL/Mssql (Nếu dùng PostgreSQL hãy đổi thành RANDOM())
    @Query(value = "SELECT * FROM menu_items WHERE is_active = true ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<MenuItem> findRandomMenuItemsActive(@Param("limit") int limit);

    // 15. [Modifying] Xóa các món ăn có số lượng bán bằng 0 và đã tắt kích hoạt từ lâu (Dọn dẹp DB)
    @Modifying
    @Transactional
    @Query("DELETE FROM MenuItem m WHERE m.soldCount = 0 AND m.isActive = false")
    int deleteUnusedAndInactiveMenuItems();
}

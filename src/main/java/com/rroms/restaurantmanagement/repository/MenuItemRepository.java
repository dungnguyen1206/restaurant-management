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
    List<MenuItem> findTop5ByOrderBySoldCountDesc();

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
}

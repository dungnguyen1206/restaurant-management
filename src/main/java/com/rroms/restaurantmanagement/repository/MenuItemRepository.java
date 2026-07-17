package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.MenuItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    @Query(value = "SELECT mi FROM MenuItem mi LEFT JOIN FETCH mi.category WHERE mi.itemName LIKE %:name% AND mi.category.categoryId = :categoryId",
           countQuery = "SELECT COUNT(mi) FROM MenuItem mi WHERE mi.itemName LIKE %:name% AND mi.category.categoryId = :categoryId")
    Page<MenuItem> findByNameAndCategory(@Param("name") String name, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query(value = "SELECT mi FROM MenuItem mi LEFT JOIN FETCH mi.category WHERE mi.itemName LIKE %:name%",
           countQuery = "SELECT COUNT(mi) FROM MenuItem mi WHERE mi.itemName LIKE %:name%")
    Page<MenuItem> findByName(@Param("name") String name, Pageable pageable);

    @Query(value = "SELECT mi FROM MenuItem mi LEFT JOIN FETCH mi.category WHERE mi.category.categoryId = :categoryId",
           countQuery = "SELECT COUNT(mi) FROM MenuItem mi WHERE mi.category.categoryId = :categoryId")
    Page<MenuItem> findByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query(value = "SELECT mi FROM MenuItem mi LEFT JOIN FETCH mi.category",
           countQuery = "SELECT COUNT(mi) FROM MenuItem mi")
    Page<MenuItem> findAllWithCategory(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT mi FROM MenuItem mi WHERE mi.itemId IN :itemIds ORDER BY mi.itemId")
    List<MenuItem> findAllByIdForUpdate(@Param("itemIds") Collection<Long> itemIds);
}

package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.MenuItem;
import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long>, JpaSpecificationExecutor<MenuItem> {



    @Query("select i from MenuItem i left join fetch i.category c" +
            " Where (:filterKey is null or lower(i.itemName) like :filterKey)" +
            " And (:categoryId is null or c.categoryId=:categoryId)")
    Page<MenuItem> findAllMenuItems(@Param("filterKey") String filterKey,@Param("categoryId") Long categoryId,Pageable pageable);

    boolean existsByItemNameIgnoreCase(String itemName);


    @Query("select i from MenuItem  i left join fetch i.category c where i.itemId =:id")
    Optional<MenuItem> findMenuItemByItemId(@Param("id") Long id);


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

    @Query("""
            SELECT oi.menuItem FROM OrderItem oi
            WHERE oi.order.status <> :excludedStatus
            GROUP BY oi.menuItem
            ORDER BY SUM(oi.quantity) DESC
            """)
    List<MenuItem> findMostPopular(@Param("excludedStatus") OrderStatus excludedStatus, Pageable pageable);

}

package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {



    @Query("select i from MenuItem i left join fetch i.category c" +
            " Where (:filterKey is null or lower(i.itemName) like :filterKey)" +
            " And (:categoryId is null or c.categoryId=:categoryId)")
    Page<MenuItem> findAllMenuItems(@Param("filterKey") String filterKey,@Param("categoryId") Long categoryId,Pageable pageable);

    boolean existsByItemNameIgnoreCase(String itemName);


    @Query("select i from MenuItem  i left join fetch i.category c where i.itemId =:id")
    Optional<MenuItem> findMenuItemByItemId(@Param("id") Long id);


}

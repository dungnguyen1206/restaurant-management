package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.rroms.restaurantmanagement.entity.constant.RoleName;
import java.util.List;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("""
        SELECT u
        FROM User u
        JOIN FETCH u.role
        WHERE u.username = :username
        """)
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);


    @Query("""
    SELECT u
    FROM User u
    JOIN FETCH u.role
    WHERE u.role.roleName = :roleName
    ORDER BY u.firstName, u.middleName, u.lastName
    """)
    List<User> findByRoleName(@Param("roleName") RoleName roleName);
}
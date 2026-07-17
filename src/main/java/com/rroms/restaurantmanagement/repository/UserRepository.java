package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("""
        SELECT u
        FROM User u
        JOIN FETCH u.role
        WHERE u.username = :username
        """)
    Optional<User> findByUsername(String username);

    @Query("""
        SELECT u
        FROM User u
        JOIN FETCH u.role
        WHERE LOWER(u.username) = LOWER(:username)
        """)
    Optional<User> findByUsernameIgnoreCase(@Param("username") String username);

    boolean existsByUsername(String username);

}

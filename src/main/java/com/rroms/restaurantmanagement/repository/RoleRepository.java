package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.Role;
import com.rroms.restaurantmanagement.entity.constant.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Role findByRoleName(RoleName roleName);
}

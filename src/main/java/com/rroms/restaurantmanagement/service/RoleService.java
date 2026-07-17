package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.entity.Role;
import com.rroms.restaurantmanagement.entity.constant.RoleName;

public interface RoleService {
    Role findByRoleName(RoleName roleName);
}

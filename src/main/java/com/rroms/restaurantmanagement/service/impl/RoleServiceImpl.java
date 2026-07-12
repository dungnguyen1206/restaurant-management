package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.entity.Role;
import com.rroms.restaurantmanagement.entity.constant.RoleName;
import com.rroms.restaurantmanagement.repository.RoleRepository;
import com.rroms.restaurantmanagement.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    @Override
    public Role findByRoleName(RoleName roleName) {
        return this.roleRepository.findByRoleName(roleName);
    }
}

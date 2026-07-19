package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.dto.request.UserRegisterDTO;
import com.rroms.restaurantmanagement.dto.request.ProfileUpdateDTO;
import com.rroms.restaurantmanagement.entity.User;
import com.rroms.restaurantmanagement.entity.constant.RoleName;
import com.rroms.restaurantmanagement.entity.constant.UserStatus;
import com.rroms.restaurantmanagement.exception.DuplicateEmailException;
import com.rroms.restaurantmanagement.exception.PasswordNotMatchException;
import com.rroms.restaurantmanagement.mapper.UserMapper;
import com.rroms.restaurantmanagement.repository.UserRepository;
import com.rroms.restaurantmanagement.service.RoleService;
import com.rroms.restaurantmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    @Override
    public User handleCreateUser(UserRegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new PasswordNotMatchException();
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateEmailException();
        }
        User user = userMapper.toEntity(dto);

        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(this.roleService.findByRoleName(RoleName.CUSTOMER));

        return userRepository.save(user);
    }

    @Override
    public User updateProfile(Long userId, ProfileUpdateDTO profile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        user.setFirstName(clean(profile.getFirstName()));
        user.setMiddleName(clean(profile.getMiddleName()));
        user.setLastName(profile.getLastName().trim());
        user.setPhone(profile.getPhone().trim());
        return userRepository.save(user);
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

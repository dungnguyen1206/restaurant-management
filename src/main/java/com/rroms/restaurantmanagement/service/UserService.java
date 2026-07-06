package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.dto.request.UserRegisterDTO;
import com.rroms.restaurantmanagement.entity.User;

public interface UserService {
    public User handleCreateUser(UserRegisterDTO user);

}

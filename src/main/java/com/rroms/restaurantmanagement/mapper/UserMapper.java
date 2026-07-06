package com.rroms.restaurantmanagement.mapper;

import com.rroms.restaurantmanagement.dto.request.UserRegisterDTO;
import com.rroms.restaurantmanagement.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "orders", ignore = true)
    User toEntity(UserRegisterDTO dto);

}
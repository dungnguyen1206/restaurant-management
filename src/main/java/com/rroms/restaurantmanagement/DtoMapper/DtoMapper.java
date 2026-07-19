package com.rroms.restaurantmanagement.DtoMapper;


import com.rroms.restaurantmanagement.dto.request.CategoryDto;
import com.rroms.restaurantmanagement.dto.request.MenuItemDto;
import com.rroms.restaurantmanagement.entity.Category;
import com.rroms.restaurantmanagement.entity.MenuItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DtoMapper {

    @Mapping(
            target = "categoryId",
            source = "category.categoryId"
    )

    @Mapping(
            target = "categoryName",
            source = "category.categoryName"
    )
    MenuItemDto toMenuItemDto(MenuItem menuItem);

    CategoryDto toCategoryDto(Category category);


}

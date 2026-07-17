package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.dto.response.CategoryResponseForManager;
import com.rroms.restaurantmanagement.entity.Category;
import com.rroms.restaurantmanagement.repository.CategoryRepository;
import com.rroms.restaurantmanagement.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    @Override
    public List<CategoryResponseForManager> findAll() {
        return  toCategoryResponseForManagers(categoryRepository.findAll());
    }

    private CategoryResponseForManager toCategoryResponseForManager(Category category) {
        return CategoryResponseForManager.builder().id(category.getCategoryId()).categoryName(category.getCategoryName()).build();
    }

    private List<CategoryResponseForManager> toCategoryResponseForManagers(List<Category> categories) {
        return categories.stream().map(this::toCategoryResponseForManager).toList();
    }


}

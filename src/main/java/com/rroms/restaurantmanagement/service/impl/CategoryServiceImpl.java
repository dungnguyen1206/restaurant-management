package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.dto.response.CategoryForWaiterResponse;
import com.rroms.restaurantmanagement.DtoMapper.DtoMapper;
import com.rroms.restaurantmanagement.dto.request.CategoryDto;
import com.rroms.restaurantmanagement.dto.response.CategoryResponseForManager;
import com.rroms.restaurantmanagement.entity.Category;
import com.rroms.restaurantmanagement.repository.CategoryRepository;
import com.rroms.restaurantmanagement.service.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final DtoMapper dtoMapper;


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

    @Override
    public List<CategoryDto> findAllForCustomer() {

        List<Category> categories = categoryRepository.findAll();

        List<CategoryDto> dtos = new ArrayList<>();
        for(Category cate : categories){
            dtos.add(dtoMapper.toCategoryDto(cate));
        }

        return dtos;
    }

    @Override
    public List<CategoryForWaiterResponse> getCategoriesforWaiter() {
         return toCategoriesForWaiter(categoryRepository.findAll());
    }

    private CategoryForWaiterResponse toCategoryForWaiter(Category category){
        return CategoryForWaiterResponse.builder().categoryId(category.getCategoryId()).categoryName(category.getCategoryName()).build();
    }

    private List<CategoryForWaiterResponse> toCategoriesForWaiter(List<Category> categories){
        return categories.stream().map(this::toCategoryForWaiter).toList();
    }


}

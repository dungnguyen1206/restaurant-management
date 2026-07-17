package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.DtoMapper.DtoMapper;
import com.rroms.restaurantmanagement.dto.request.CategoryDto;
import com.rroms.restaurantmanagement.entity.Category;
import com.rroms.restaurantmanagement.repository.CategoryRepository;
import com.rroms.restaurantmanagement.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final DtoMapper dtoMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, DtoMapper dtoMapper)
    {
        this.categoryRepository = categoryRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public List<CategoryDto> findAll() {

        List<Category> categories = categoryRepository.findAll();

        List<CategoryDto> dtos = new ArrayList<>();
        for(Category cate : categories){
            dtos.add(dtoMapper.toCategoryDto(cate));
        }

        return dtos;
    }
}

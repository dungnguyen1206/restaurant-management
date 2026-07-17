package com.rroms.restaurantmanagement.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    String uploadImage(MultipartFile file);
    void deleteImage(String imageId);
}

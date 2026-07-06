package com.rroms.restaurantmanagement.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rroms.restaurantmanagement.service.CloudinaryService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file) {
        try{
            Map uploadImage= cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", "RROMS",
                    "resource_type", "image"));
            return uploadImage.get("secret_url").toString();
        }
        catch (Exception ex){
            throw new RuntimeException("Tải ảnh lên thất bại " +ex.getMessage());
        }
    }

    @Override
    public void deleteImage(String imageId) {
        try{
            cloudinary.uploader().destroy(imageId,ObjectUtils.emptyMap());
        }
        catch (Exception ex){
            throw  new  RuntimeException("Xóa ảnh thất bại" +ex.getMessage());
        }
    }


}

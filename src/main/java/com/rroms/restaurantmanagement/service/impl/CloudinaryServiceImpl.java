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
            Map<String,Object>uploadImage= cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", "RROMS",
                    "resource_type", "image"));
            return uploadImage.get("secure_url").toString();
        }
        catch (Exception ex){
            throw new RuntimeException("Tải ảnh lên thất bại " +ex.getMessage());
        }
    }

    @Override
    public void deleteImage(String imageId) {

        if(imageId == null || !imageId.contains("cloudinary.com")){
            return;
        }

        try{
           String filename = imageId.substring(imageId.lastIndexOf("/")+1);
           String publicId = imageId.substring(0,filename.lastIndexOf("."));
           cloudinary.uploader().destroy(publicId,ObjectUtils.emptyMap());
        }
        catch (Exception ex){
            throw  new  RuntimeException("Xóa ảnh thất bại" +ex.getMessage());
        }
    }


}

package com.rroms.restaurantmanagement.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> cloudinaryConfig = new HashMap<String, String>();
        cloudinaryConfig.put("cloud_name", cloudName);
        cloudinaryConfig.put("api_key", apiKey);
        cloudinaryConfig.put("api_secret", apiSecret);
        return new Cloudinary(cloudinaryConfig);
    }

}

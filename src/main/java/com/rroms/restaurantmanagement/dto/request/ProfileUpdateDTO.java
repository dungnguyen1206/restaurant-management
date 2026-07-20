package com.rroms.restaurantmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateDTO {
    @Size(max = 20, message = "Họ không được vượt quá 20 ký tự")
    private String firstName;

    @Size(max = 30, message = "Tên đệm không được vượt quá 30 ký tự")
    private String middleName;

    @NotBlank(message = "Vui lòng nhập tên")
    @Size(max = 20, message = "Tên không được vượt quá 20 ký tự")
    private String lastName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$", message = "Số điện thoại không hợp lệ")
    private String phone;
}

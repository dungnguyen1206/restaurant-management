package com.rroms.restaurantmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {

    @Size(max = 10, message = "Họ không được quá 10 kí tự")
    private String firstName;

    @Size(max = 20, message = "Tên đệm không dược quá 20 kí tự")
    private String middleName;

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 10, message = "Tên không được quá 10 kí tự")
    private String lastName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String username;

    @NotBlank(message = "Số điện thoại không được để trống!")
    @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Xác nhận mật khẩu phải có ít nhất 6 kí tự")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    @Size(min = 6, message = "Xác nhận mật khẩu phải có ít nhất 6 kí tự")
    private String confirmPassword;
}
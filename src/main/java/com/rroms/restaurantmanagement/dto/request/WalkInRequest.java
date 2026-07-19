package com.rroms.restaurantmanagement.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WalkInRequest {
    @NotEmpty(message = "Vui lòng chọn ít nhất một bàn")
    private List<Long> tableIds = new ArrayList<>();

    @Size(max = 20, message = "Họ không được quá 20 ký tự")
    private String firstName;

    @Size(max = 30, message = "Tên đệm không được quá 30 ký tự")
    private String middleName;

    @NotBlank(message = "Tên khách không được để trống")
    @Size(max = 20, message = "Tên không được quá 20 ký tự")
    private String lastName;

    @NotNull(message = "Số lượng khách không được để trống")
    @Min(value = 1, message = "Số lượng khách phải lớn hơn 0")
    private Integer numberOfGuests;

    @Size(max = 500, message = "Ghi chú không được quá 500 ký tự")
    private String note;
}

package com.rroms.restaurantmanagement.dto.request;

import lombok.Data;

@Data
public class WalkInRequest {
    private Long tableId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private Integer numberOfGuests;
    private String note;
}
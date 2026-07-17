package com.rroms.restaurantmanagement.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("Email đã tồn tại");
    }
}

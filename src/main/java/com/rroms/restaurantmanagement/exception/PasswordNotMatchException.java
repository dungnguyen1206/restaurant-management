package com.rroms.restaurantmanagement.exception;

public class PasswordNotMatchException extends RuntimeException {
    public PasswordNotMatchException() {
        super("Mật khẩu xác nhận không khớp");
    }
}

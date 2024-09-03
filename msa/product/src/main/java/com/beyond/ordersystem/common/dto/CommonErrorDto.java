package com.beyond.ordersystem.common.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class CommonErrorDto {
    private int status_code;
    private String error_message;

    public CommonErrorDto(HttpStatus status_code, String error_message) {
        this.status_code = status_code.value();
        this.error_message = error_message;
    }
}

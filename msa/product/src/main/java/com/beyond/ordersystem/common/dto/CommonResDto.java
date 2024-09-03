package com.beyond.ordersystem.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class CommonResDto {
    private int status_code;
    private String Status_message;
    private Object result;

    public CommonResDto(HttpStatus status_code, String status_message, Object result) {
        this.status_code = status_code.value();
        Status_message = status_message;
        this.result = result;
    }
}

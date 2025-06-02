package org.example.aiodataservice.application.dtos;

import lombok.Getter;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MyResponse<T> {
    private final boolean isSuccess;
    private final List<String> errors;
    private final String message;
    private final int statusCode;
    private final T data;

    private MyResponse(boolean isSuccess, List<String> errors, String message, int statusCode, T data) {
        this.isSuccess = isSuccess;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.message = message;
        this.statusCode = statusCode;
        this.data = data;
    }

    public static <T> MyResponse<T> success(String message) {
        return new MyResponse<>(true, new ArrayList<>(), message, HttpStatus.SC_OK, null);
    }

    public static <T> MyResponse<T> success(String message, T data) {
        return new MyResponse<>(true, new ArrayList<>(), message, HttpStatus.SC_OK, data);
    }

    public static <T> MyResponse<T> error(List<String> errors, String message) {
        return new MyResponse<>(false, errors, message, HttpStatus.SC_BAD_REQUEST, null);
    }

    public static <T> MyResponse<T> error(List<String> errors, String message, int statusCode) {
        return new MyResponse<>(false, errors, message, statusCode, null);
    }
}

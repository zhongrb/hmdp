package com.hmdp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        return new Result<>(true, "成功", data);
    }

    public static Result<Void> ok() {
        return new Result<>(true, "成功", null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(false, message, null);
    }
}

package com.hmdp.exception;

import com.hmdp.dto.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleUnauthorized(UnauthorizedException exception) {
        // 未登录场景统一返回 401，前端据此决定弹登录引导还是跳转登录页。
        return Result.fail(exception.getMessage());
    }

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBiz(BizException exception) {
        // 业务异常保留中文原始提示，方便前端直接展示明确反馈。
        return Result.fail(exception.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(Exception exception) {
        // 参数校验类异常统一收口，避免把底层校验细节直接暴露给前端。
        return Result.fail("请求参数不合法");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleOther(Exception exception) {
        // 未分类异常统一隐藏内部细节，对外只暴露通用失败提示。
        return Result.fail("系统繁忙，请稍后重试");
    }
}

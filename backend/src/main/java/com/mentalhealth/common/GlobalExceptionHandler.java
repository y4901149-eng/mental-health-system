package com.mentalhealth.common;

import com.mentalhealth.vo.ResultVO;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理器
 * 作用：统一捕获后端抛出的异常，返回标准 JSON 格式给前端
 * 这样前端不用处理各种异常类型，只看 code 和 message 就行
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 处理业务异常（Service层手动抛出的 RuntimeException） */
    @ExceptionHandler(RuntimeException.class)
    public ResultVO<?> handleRuntimeException(RuntimeException e) {
        return ResultVO.badRequest(e.getMessage());
    }

    /** 处理参数校验异常（@Valid 校验失败时触发） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultVO<?> handleValidation(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> errors = bindingResult.getFieldErrors();
        StringBuilder message = new StringBuilder();
        for (FieldError error : errors) {
            message.append(error.getDefaultMessage()).append("；");
        }
        return ResultVO.badRequest(message.toString());
    }

    /** 处理其他未捕获的异常 */
    @ExceptionHandler(Exception.class)
    public ResultVO<?> handleException(Exception e) {
        e.printStackTrace();
        return ResultVO.serverError();
    }
}

package com.rokyai.dnd14th1backend.common.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import lombok.extern.slf4j.Slf4j;

import com.rokyai.dnd14th1backend.common.response.ApiExceptionResponse;
import com.rokyai.dnd14th1backend.common.response.DefaultStatus;

/** 전역 예외 처리 핸들러. */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiExceptionResponse<String>> handleApiException(ApiException ex) {
        log.warn(
                "API 예외 발생 [Status] : {}, [Detail] : {}\n[StackTrace] : \n{}",
                ex.getStatus(),
                ex.getDetail(),
                ex.getStackTrace());
        ApiExceptionResponse<String> response =
                new ApiExceptionResponse<>(ex.getStatus(), null, ex.getDetail());
        return ResponseEntity.status(ex.getStatus().getHttpStatusCode()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse<List<String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        List<String> errorMessageList =
                ex.getFieldErrors().stream()
                        .map(
                                error ->
                                        String.format(
                                                "%s : { %s } 은 %s",
                                                error.getField(),
                                                error.getRejectedValue(),
                                                error.getDefaultMessage()))
                        .toList();

        return ApiExceptionResponse.error(DefaultStatus.BAD_REQUEST, errorMessageList);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse<List<String>> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex) {
        List<String> errorMessageList =
                ex.getAllErrors().stream()
                        .map(
                                error ->
                                        error.getDefaultMessage() != null
                                                ? error.getDefaultMessage()
                                                : error.toString())
                        .toList();

        return ApiExceptionResponse.error(DefaultStatus.BAD_REQUEST, errorMessageList);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiExceptionResponse<String> handleAllExceptions(Exception ex) {
        log.error(
                "예외 발생: [Type] : {}, [Message] : {}\n[StackTrace] : \n{}",
                ex.getClass().getName(),
                ex.getMessage(),
                ex.getStackTrace());
        return ApiExceptionResponse.error(DefaultStatus.UNKNOWN_ERROR);
    }
}

package com.example.url_shortner.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(FunctionalException.class)
    public ResponseEntity<String> handleFunctionalException(FunctionalException exception) {
        return ResponseEntity.status(exception.getHttpStatus()).body(exception.getMessage());
    }

}

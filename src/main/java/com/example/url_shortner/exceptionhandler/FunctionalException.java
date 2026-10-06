package com.example.url_shortner.exceptionhandler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class FunctionalException extends RuntimeException {
    private final HttpStatus httpStatus;
    public FunctionalException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }
}

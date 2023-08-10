package com.example.springboot.exception.exceptions;

import java.io.IOException;

import lombok.Getter;

@Getter
public class NotFoundException extends IOException {
    private final String detail;

    public NotFoundException(String message) {
        super(message);
        this.detail = "%s.%s()".formatted(
                Thread.currentThread().getStackTrace()[2].getClassName(),
                Thread.currentThread().getStackTrace()[2].getMethodName());
    }
}

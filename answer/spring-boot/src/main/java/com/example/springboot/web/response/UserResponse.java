package com.example.springboot.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private String name;
    private Integer age;
}

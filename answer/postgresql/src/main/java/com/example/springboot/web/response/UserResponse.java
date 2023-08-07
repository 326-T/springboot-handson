package com.example.springboot.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Integer id;
    private String name;
    private String email;
}

package com.example.springboot.web.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserIndexResponse {

    private List<UserResponse> data;
}

package com.example.springboot.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SampleResponse {
    private String name;
    private Integer age;
}

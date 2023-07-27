package com.example.springboot.web.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BMIRequest {
    private Double height;
    private Double weight;
}

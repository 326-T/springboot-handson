package com.example.springboot.service;

import org.springframework.stereotype.Service;

@Service
public class SampleService {

    public Integer square(Integer number) {
        return number * number;
    }

    public Double bmi(Double weight, Double height) {
        return weight / height / height;
    }
}

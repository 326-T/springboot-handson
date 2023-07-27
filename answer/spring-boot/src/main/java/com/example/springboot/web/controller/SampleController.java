package com.example.springboot.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot.service.SampleService;
import com.example.springboot.web.request.BMIRequest;
import com.example.springboot.web.request.UserRequest;
import com.example.springboot.web.response.UserResponse;

@RestController
@RequestMapping("/sample")
public class SampleController {

    @Autowired
    private SampleService sampleService;

    @GetMapping
    public String hello() {
        return "Hello World!";
    }

    @GetMapping("/path/{id}")
    public String path(@PathVariable Integer id) {
        return "パスパラメータで %d を受け取りました".formatted(id);
    }

    @GetMapping("/query")
    public String query(@RequestParam Integer id) {
        return "クエリパラメータで %d を受け取りました".formatted(id);
    }

    @PostMapping("/body")
    public String body(@RequestBody Integer id) {
        return "リクエストボディで %d を受け取りました".formatted(id);
    }

    @PostMapping("/user")
    public String user(@RequestBody UserRequest userRequest) {
        return "リクエストボディで 名前:%s, 年齢:%d を受け取りました".formatted(userRequest.getName(), userRequest.getAge());
    }

    @GetMapping("/user/{id}")
    public UserResponse user(@PathVariable Integer id) {
        return UserResponse.builder().name("伊藤").age(20).build();
    }

    @GetMapping("/square/{number}")
    public Integer square(@PathVariable Integer number) {
        return number * number;
    }

    @PostMapping("/bmi")
    public Double bmi(@RequestBody BMIRequest bmiRequest) {
        return sampleService.bmi(bmiRequest.getWeight(), bmiRequest.getHeight());
    }
}

package com.example.springboot.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot.exception.exceptions.NotFoundException;
import com.example.springboot.persistence.entity.User;
import com.example.springboot.service.UserService;
import com.example.springboot.web.request.UserRequest;
import com.example.springboot.web.response.UserIndexResponse;
import com.example.springboot.web.response.UserResponse;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public UserIndexResponse index() {
        List<User> userList = userService.findAll();
        return UserIndexResponse.builder().data(
                userList.stream().map(user -> UserResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .build()).toList())
                .build();
    }

    @GetMapping("/id/{id}")
    public UserResponse findById(HttpServletResponse httpServletResponse, @PathVariable Integer id)
            throws NotFoundException {
        User user = userService.findById(id);
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Integer id, @RequestBody UserRequest userRequest) {
        userService.update(User.builder().id(id).name(userRequest.getName()).email(userRequest.getEmail()).build());
    }

    @PutMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void insert(HttpServletResponse httpServletResponse, @RequestBody UserRequest userRequest) {
        User user = User.builder().name(userRequest.getName()).email(userRequest.getEmail()).build();
        userService.insert(user);
        httpServletResponse.setHeader("Location", "/api/user/%d".formatted(user.getId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Integer id) {
        userService.deleteById(id);
    }
}

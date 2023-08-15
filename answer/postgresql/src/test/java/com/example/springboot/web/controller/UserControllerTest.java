package com.example.springboot.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.springboot.exception.exceptions.NotFoundException;
import com.example.springboot.persistence.entity.User;
import com.example.springboot.service.UserService;
import com.example.springboot.web.response.ErrorResponse;
import com.example.springboot.web.response.UserIndexResponse;
import com.example.springboot.web.response.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @Test
    void index() throws Exception {
        // given
        List<User> users = List.of(
                User.builder().id(1).name("太郎").email("xxx@example.com").build(),
                User.builder().id(2).name("次郎").email("yyy@example.com").build());
        UserIndexResponse expected = UserIndexResponse.builder().data(List.of(
                UserResponse.builder().id(1).name("太郎").email("xxx@example.com").build(),
                UserResponse.builder().id(2).name("次郎").email("yyy@example.com").build())).build();
        when(userService.findAll()).thenReturn(users);
        // when, then
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
    }

    @Nested
    class findById {
        @Test
        void ok() throws Exception {
            // given
            User user = User.builder().id(1).name("太郎").email("xxx@example.com").build();
            UserResponse expected = UserResponse.builder().id(1).name("太郎").email("xxx@example.com")
                    .build();
            when(userService.findById(1)).thenReturn(user);
            // when, then
            mockMvc.perform(get("/api/user/id/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(expected)));
        }

        @Test
        void ng() throws Exception {
            // given
            ErrorResponse expected = ErrorResponse.builder().message("not found").build();
            when(userService.findById(99)).thenThrow(new NotFoundException("not found"));
            // when, then
            mockMvc.perform(get("/api/user/id/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().json(mapper.writeValueAsString(expected)));
        }
    }

    @Test
    void update() throws Exception {
        // given
        Map<String, String> userRequestMap = new HashMap<>();
        userRequestMap.put("name", "太郎");
        userRequestMap.put("email", "xxx@example.com");
        doNothing().when(userService).update(any(User.class));
        // when, then
        mockMvc.perform(post("/api/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userRequestMap)))
                .andExpect(status().isNoContent());
    }

    @Test
    void insert() throws Exception {
        // given
        Map<String, String> userRequestMap = new HashMap<>();
        userRequestMap.put("name", "太郎");
        userRequestMap.put("email", "xxx@example.com");
        doNothing().when(userService).insert(any(User.class));
        // when, then
        mockMvc.perform(put("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userRequestMap)))
                .andExpect(status().isCreated());
    }

    @Nested
    class deleteById {
        @Test
        void ok() throws Exception {
            // given
            doNothing().when(userService).deleteById(1);
            // when, then
            mockMvc.perform(delete("/api/user/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }

        @Test
        void ng() throws Exception {
            // given
            ErrorResponse expected = ErrorResponse.builder().message("not found").build();
            doThrow(new NotFoundException("not found")).when(userService).deleteById(99);
            // when, then
            mockMvc.perform(delete("/api/user/99")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().json(mapper.writeValueAsString(expected)));
        }
    }
}

package com.example.springboot.web.controller;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.springboot.persistence.entity.Comment;
import com.example.springboot.service.CommentService;
import com.example.springboot.web.response.CommentIndexResponse;
import com.example.springboot.web.response.CommentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CommentController.class)
public class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    void findAll() throws Exception {
        // given
        List<Comment> commentList = List.of(
                Comment.builder().id("1").role("user").content("こんにちは")
                        .createdAt(LocalDateTime.parse("2023-08-01T00:00:00.000"))
                        .updatedAt(LocalDateTime.parse("2023-08-02T00:00:00.000")).version(1L).build(),
                Comment.builder().id("2").role("user").content("こんばんは")
                        .createdAt(LocalDateTime.parse("2023-08-01T00:00:00.000"))
                        .updatedAt(LocalDateTime.parse("2023-08-02T00:00:00.000")).version(1L).build());
        CommentIndexResponse expected = CommentIndexResponse.builder().count(2L).comments(List.of(
                CommentResponse.builder().id("1").role("user").content("こんにちは")
                        .createdAt(LocalDateTime.parse("2023-08-01T00:00:00.000"))
                        .updatedAt(LocalDateTime.parse("2023-08-02T00:00:00.000")).version(1L).build(),
                CommentResponse.builder().id("2").role("user").content("こんばんは")
                        .createdAt(LocalDateTime.parse("2023-08-01T00:00:00.000"))
                        .updatedAt(LocalDateTime.parse("2023-08-02T00:00:00.000")).version(1L).build()))
                .build();
        when(commentService.count()).thenReturn(2L);
        when(commentService.findAll()).thenReturn(commentList);
        // when, then
        mockMvc.perform(get("/api/comment"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
    }

    @Nested
    class findById {
        @Test
        void OK() throws Exception {
            // given
            Comment comment = Comment.builder().id("1").role("user").content("こんにちは")
                    .createdAt(LocalDateTime.parse("2023-08-01T00:00:00.000"))
                    .updatedAt(LocalDateTime.parse("2023-08-02T00:00:00.000")).version(1L).build();
            CommentResponse expected = CommentResponse.builder().id("1").role("user").content("こんにちは")
                    .createdAt(LocalDateTime.parse("2023-08-01T00:00:00.000"))
                    .updatedAt(LocalDateTime.parse("2023-08-02T00:00:00.000")).version(1L).build();
            when(commentService.findById("1")).thenReturn(comment);
            // when, then
            mockMvc.perform(get("/api/comment/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(expected)));
        }

        @Test
        void NOT_FOUND() throws Exception {
            // given
            when(commentService.findById("99")).thenReturn(null);
            // when, then
            mockMvc.perform(get("/api/comment/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class update {
        @Test
        void OK() throws Exception {
            // given
            Map<String, String> commentRequestMap = new HashMap<>();
            commentRequestMap.put("role", "user");
            commentRequestMap.put("content", "こんにちは");
            Comment comment = Comment.builder().id("1").role("user").content("こんにちは")
                    .createdAt(LocalDateTime.parse("2023-08-01T00:00:00.000"))
                    .updatedAt(LocalDateTime.parse("2023-08-02T00:00:00.000")).version(1L).build();
            CommentResponse expected = CommentResponse.builder().id("1").role("user").content("こんにちは")
                    .createdAt(LocalDateTime.parse("2023-08-01T00:00:00.000"))
                    .updatedAt(LocalDateTime.parse("2023-08-02T00:00:00.000")).version(1L).build();
            when(commentService.findById("1")).thenReturn(comment);
            when(commentService.save(any(Comment.class))).thenReturn(comment);
            // when, then
            mockMvc.perform(post("/api/comment/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commentRequestMap)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(expected)));
        }

        @Test
        void NOT_FOUND() throws Exception {
            // given
            Map<String, String> commentRequestMap = new HashMap<>();
            commentRequestMap.put("role", "user");
            commentRequestMap.put("content", "こんにちは");
            when(commentService.findById("1")).thenReturn(null);
            when(commentService.save(any(Comment.class))).thenReturn(null);
            // when, then
            mockMvc.perform(post("/api/comment/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commentRequestMap)))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void insert() throws Exception {
        // given
        Map<String, String> commentRequestMap = new HashMap<>();
        commentRequestMap.put("role", "user");
        commentRequestMap.put("content", "こんにちは");
        Comment comment = Comment.builder().id("1").role("user").content("こんにちは")
                .createdAt(LocalDateTime.parse("2023-08-01T00:00:00.000"))
                .updatedAt(LocalDateTime.parse("2023-08-02T00:00:00.000")).version(1L).build();
        when(commentService.save(any(Comment.class))).thenReturn(comment);
        // when, then
        mockMvc.perform(put("/api/comment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequestMap)))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteById() throws Exception {
        // given
        doNothing().when(commentService).deleteById("1");
        // when, then
        mockMvc.perform(delete("/api/comment/1"))
                .andExpect(status().isNoContent());
    }
}

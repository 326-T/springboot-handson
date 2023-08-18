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

import com.example.springboot.persistence.entity.Comment;
import com.example.springboot.service.CommentService;
import com.example.springboot.web.request.CommentRequest;
import com.example.springboot.web.response.CommentIndexResponse;
import com.example.springboot.web.response.CommentResponse;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CommentIndexResponse findAll() {
        return CommentIndexResponse.builder()
                .count(commentService.count())
                .comments(commentService.findAll().stream()
                        .map(this::map)
                        .toList())
                .build();
    }

    @GetMapping("/{id}")
    public CommentResponse findById(HttpServletResponse httpServletResponse, @PathVariable String id) {
        Comment comment = commentService.findById(id);
        if (comment == null) {
            httpServletResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }
        httpServletResponse.setStatus(HttpStatus.OK.value());
        return map(comment);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse insert(@RequestBody CommentRequest commentRequest) {
        return map(commentService.save(Comment.builder()
                .role(commentRequest.getRole())
                .content(commentRequest.getContent())
                .build()));
    }

    @PutMapping("/{id}")
    public CommentResponse update(HttpServletResponse httpServletResponse, @PathVariable String id,
            @RequestBody CommentRequest commentRequest) {
        Comment comment = commentService.findById(id);
        if (comment == null) {
            httpServletResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }
        httpServletResponse.setStatus(HttpStatus.OK.value());
        return map(commentService.save(Comment.builder()
                .id(id)
                .role(commentRequest.getRole())
                .content(commentRequest.getContent())
                .createdAt(comment.getCreatedAt())
                .version(comment.getVersion())
                .build()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable String id) {
        commentService.deleteById(id);
    }

    private CommentResponse map(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .role(comment.getRole())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .version(comment.getVersion())
                .build();
    }
}

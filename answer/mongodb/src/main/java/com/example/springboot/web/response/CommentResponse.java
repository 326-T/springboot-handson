package com.example.springboot.web.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponse {
    private String id;
    private String role;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}

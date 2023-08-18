package com.example.springboot.web.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private String id;
    private String role;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}

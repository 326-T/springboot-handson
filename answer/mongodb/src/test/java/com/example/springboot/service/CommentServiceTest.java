package com.example.springboot.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.springboot.persistence.entity.Comment;
import com.example.springboot.persistence.repository.CommentRepository;

@SpringBootTest
public class CommentServiceTest {

        @InjectMocks
        private CommentService commentService;

        @Mock
        private CommentRepository commentRepository;

        @Test
        void save() {
                // given
                Comment user = Comment.builder().id("1").role("user").content("こんにちは")
                                .createdAt(LocalDateTime.parse("2023-08-01T00:00:00.000"))
                                .updatedAt(LocalDateTime.parse("2023-08-02T00:00:00.000")).version(1L).build();
                // when
                when(commentRepository.save(any(Comment.class))).thenReturn(user);
                // then
                Comment result = commentService.save(user);
                assertThat(result)
                                .extracting(Comment::getId, Comment::getRole, Comment::getContent,
                                                Comment::getCreatedAt,
                                                Comment::getUpdatedAt, Comment::getVersion)
                                .containsExactly("1", "user", "こんにちは", LocalDateTime.parse("2023-08-01T00:00:00.000"),
                                                LocalDateTime.parse("2023-08-02T00:00:00.000"), 1L);
        }

        @Test
        void findById() {
                // given
                Comment user = Comment.builder().id("1").role("user").content("こんにちは")
                                .createdAt(LocalDateTime.parse("2023-08-01T00:00:00.000"))
                                .updatedAt(LocalDateTime.parse("2023-08-02T00:00:00.000")).version(1L).build();
                // when
                when(commentRepository.findById("1")).thenReturn(Optional.of(user));
                // then
                Comment result = commentService.findById("1");
                assertThat(result)
                                .extracting(Comment::getId, Comment::getRole, Comment::getContent,
                                                Comment::getCreatedAt,
                                                Comment::getUpdatedAt, Comment::getVersion)
                                .containsExactly("1", "user", "こんにちは", LocalDateTime.parse("2023-08-01T00:00:00.000"),
                                                LocalDateTime.parse("2023-08-02T00:00:00.000"), 1L);

        }

        @Test
        void findAll() {
                // given
                List<Comment> userList = Arrays.asList(
                                Comment.builder().id("1").role("user").content("こんにちは")
                                                .createdAt(LocalDateTime.parse("2023-08-01T00:00:00.000"))
                                                .updatedAt(LocalDateTime.parse("2023-08-02T00:00:00.000")).version(1L)
                                                .build(),
                                Comment.builder().id("2").role("assistant").content("こんばんは")
                                                .createdAt(LocalDateTime.parse("2023-08-03T00:00:00.000"))
                                                .updatedAt(LocalDateTime.parse("2023-08-04T00:00:00.000")).version(2L)
                                                .build());
                // when
                when(commentRepository.findAll()).thenReturn(userList);
                // then
                List<Comment> result = commentService.findAll();
                assertThat(result).hasSize(2);
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.get(0))
                                .extracting(Comment::getId, Comment::getRole, Comment::getContent,
                                                Comment::getCreatedAt,
                                                Comment::getUpdatedAt, Comment::getVersion)
                                .containsExactly("1", "user", "こんにちは", LocalDateTime.parse("2023-08-01T00:00:00.000"),
                                                LocalDateTime.parse("2023-08-02T00:00:00.000"), 1L);
                assertThat(result.get(1))
                                .extracting(Comment::getId, Comment::getRole, Comment::getContent,
                                                Comment::getCreatedAt,
                                                Comment::getUpdatedAt, Comment::getVersion)
                                .containsExactly("2", "assistant", "こんばんは",
                                                LocalDateTime.parse("2023-08-03T00:00:00.000"),
                                                LocalDateTime.parse("2023-08-04T00:00:00.000"), 2L);
        }

    @Test
    void count() {
        // when
        when(commentRepository.count()).thenReturn(1L);
        // then
        Long result = commentService.count();
        assertThat(result).isEqualTo(1L);
    }

        @Test
        void deleteById() {
                // when
                doNothing().when(commentRepository).deleteById("1");
                // then
                commentService.deleteById("1");
                verify(commentRepository).deleteById("1");
        }

        @Test
        void deleteAll() {
                // when
                doNothing().when(commentRepository).deleteAll();
                // then
                commentService.deleteAll();
                verify(commentRepository).deleteAll();
        }
}

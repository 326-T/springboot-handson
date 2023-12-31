package com.example.springboot.it;

import com.example.springboot.persistence.entity.Comment;
import com.example.springboot.persistence.repository.CommentRepository;
import com.example.springboot.web.response.CommentIndexResponse;
import com.example.springboot.web.response.CommentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
class CommentApiTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ObjectMapper mapper;

    private final HttpHeaders httpHeaders = new HttpHeaders();

    @BeforeEach
    void setUp() {
        commentRepository.save(Comment.builder().id("1").role("user").content("こんにちは").build());
        commentRepository.save(Comment.builder().id("2").role("assistant").content("なにかお手伝いできますか").build());
        commentRepository.save(Comment.builder().id("3").role("user").content("こんばんわ").build());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
    }

    @AfterEach
    void tearDown() {
        commentRepository.deleteAll();
    }

    @Test
    void findAll() {
        // when
        ResponseEntity<CommentIndexResponse> responseEntity = testRestTemplate.exchange("/api/comment", HttpMethod.GET,
                new HttpEntity<>(httpHeaders), CommentIndexResponse.class);
        // then
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        CommentIndexResponse body = responseEntity.getBody();
        assertThat(body.getCount()).isEqualTo(3);
        assertThat(body.getComments().get(0))
                .extracting(CommentResponse::getId, CommentResponse::getRole, CommentResponse::getContent, CommentResponse::getVersion)
                .containsExactly("1", "user", "こんにちは", 0L);
        assertThat(body.getComments().get(0).getCreatedAt()).isAfter("2023-08-13T00:00:00");
        assertThat(body.getComments().get(0).getUpdatedAt()).isAfter("2023-08-13T00:00:00");

        assertThat(body.getComments().get(1))
                .extracting(CommentResponse::getId, CommentResponse::getRole, CommentResponse::getContent, CommentResponse::getVersion)
                .containsExactly("2", "assistant", "なにかお手伝いできますか", 0L);
        assertThat(body.getComments().get(1).getCreatedAt()).isAfter("2023-08-13T00:00:00");
        assertThat(body.getComments().get(1).getUpdatedAt()).isAfter("2023-08-13T00:00:00");

        assertThat(body.getComments().get(2))
                .extracting(CommentResponse::getId, CommentResponse::getRole, CommentResponse::getContent, CommentResponse::getVersion)
                .containsExactly("3", "user", "こんばんわ", 0L);
        assertThat(body.getComments().get(2).getCreatedAt()).isAfter("2023-08-13T00:00:00");
        assertThat(body.getComments().get(2).getUpdatedAt()).isAfter("2023-08-13T00:00:00");
    }

    @Test
    void findById() {
        // when
        ResponseEntity<CommentResponse> responseEntity = testRestTemplate.exchange("/api/comment/1", HttpMethod.GET,
                new HttpEntity<>(httpHeaders), CommentResponse.class);
        // then
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        CommentResponse commentResponse = responseEntity.getBody();
        assertThat(commentResponse)
                .extracting(CommentResponse::getId, CommentResponse::getRole, CommentResponse::getContent, CommentResponse::getVersion)
                .containsExactly("1", "user", "こんにちは", 0L);
    }

    @Test
    void insert() {
        // given
        Map<String, String> commentRequestMap = new HashMap<>();
        commentRequestMap.put("role", "user");
        commentRequestMap.put("content", "こんにちは");
        // when
        ResponseEntity<CommentResponse> responseEntity = testRestTemplate.exchange("/api/comment", HttpMethod.POST,
                new HttpEntity<>(commentRequestMap, httpHeaders), CommentResponse.class);
        // then
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(201);
        CommentResponse commentResponse = responseEntity.getBody();
        assertThat(commentResponse.getId()).isNotBlank();
        assertThat(commentResponse)
                .extracting(CommentResponse::getRole, CommentResponse::getContent, CommentResponse::getVersion)
                .containsExactly("user", "こんにちは", 0L);
    }

    @Test
    void update() {
        // given
        Map<String, String> commentRequestMap = new HashMap<>();
        commentRequestMap.put("role", "user");
        commentRequestMap.put("content", "こんばんわ");
        // when
        ResponseEntity<CommentResponse> responseEntity = testRestTemplate.exchange("/api/comment/1", HttpMethod.PUT,
                new HttpEntity<>(commentRequestMap, httpHeaders), CommentResponse.class);
        // then
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        CommentResponse commentResponse = responseEntity.getBody();
        assertThat(commentResponse)
                .extracting(CommentResponse::getId, CommentResponse::getRole, CommentResponse::getContent, CommentResponse::getVersion)
                .containsExactly("1", "user", "こんばんわ", 1L);
    }

    @Test
    void deleteById() {
        // when
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange("/api/comment/1", HttpMethod.DELETE,
                new HttpEntity<>(httpHeaders), Void.class);
        // then
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(204);

        ResponseEntity<CommentIndexResponse> result = testRestTemplate.exchange("/api/comment", HttpMethod.GET,
                new HttpEntity<>(httpHeaders), CommentIndexResponse.class);
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        CommentIndexResponse body = result.getBody();
        assertThat(body.getCount()).isEqualTo(2);
        assertThat(body.getComments().get(0))
                .extracting(CommentResponse::getId, CommentResponse::getRole, CommentResponse::getContent, CommentResponse::getVersion)
                .containsExactly("2", "assistant", "なにかお手伝いできますか", 0L);
        assertThat(body.getComments().get(0).getCreatedAt()).isAfter("2023-08-13T00:00:00");
        assertThat(body.getComments().get(0).getUpdatedAt()).isAfter("2023-08-13T00:00:00");

        assertThat(body.getComments().get(1))
                .extracting(CommentResponse::getId, CommentResponse::getRole, CommentResponse::getContent, CommentResponse::getVersion)
                .containsExactly("3", "user", "こんばんわ", 0L);
        assertThat(body.getComments().get(1).getCreatedAt()).isAfter("2023-08-13T00:00:00");
        assertThat(body.getComments().get(1).getUpdatedAt()).isAfter("2023-08-13T00:00:00");
    }
}

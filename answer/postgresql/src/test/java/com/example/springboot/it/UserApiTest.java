package com.example.springboot.it;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.springboot.web.response.ErrorResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.springboot.Application;
import com.example.springboot.listener.FlywayTestExecutionListener;
import com.example.springboot.web.response.UserResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestExecutionListeners(listeners = { FlywayTestExecutionListener.class }, mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
class UserApiTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper mapper;
    private final HttpHeaders httpHeaders = new HttpHeaders();

    @BeforeAll
    void setUp() {
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
    }

    @Nested
    class Index {
        @Test
        void success() throws JsonProcessingException {
            // when
            ResponseEntity<String> responseEntity = restTemplate.exchange("/api/user", HttpMethod.GET,
                    new HttpEntity<>(httpHeaders), String.class);
            Map<String, List<UserResponse>> body = mapper.readValue(responseEntity.getBody(),
                    new TypeReference<>() {
                    });
            // then
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
            assertThat(body.get("data")).hasSize(3);
            assertThat(body.get("data").get(0))
                    .extracting(UserResponse::getId, UserResponse::getName, UserResponse::getEmail)
                    .containsExactly(1, "太郎", "xxx@example.com");
            assertThat(body.get("data").get(1))
                    .extracting(UserResponse::getId, UserResponse::getName, UserResponse::getEmail)
                    .containsExactly(2, "次郎", "yyy@example.com");
            assertThat(body.get("data").get(2))
                    .extracting(UserResponse::getId, UserResponse::getName, UserResponse::getEmail)
                    .containsExactly(3, "三郎", "zzz@example.com");
        }
    }

    @Nested
    @Order(1)
    class FindById {
        @Test
        void 最初のレコード取得() {
            // when
            ResponseEntity<UserResponse> responseEntity = restTemplate.exchange("/api/user/id/1", HttpMethod.GET,
                    new HttpEntity<>(httpHeaders), UserResponse.class);
            // then
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
            assertThat(responseEntity.getBody())
                    .extracting(UserResponse::getId, UserResponse::getName, UserResponse::getEmail)
                    .containsExactly(1, "太郎", "xxx@example.com");
        }

        @Test
        void 二番目のレコード取得() {
            // when
            ResponseEntity<UserResponse> responseEntity = restTemplate.exchange("/api/user/id/2", HttpMethod.GET,
                    new HttpEntity<>(httpHeaders), UserResponse.class);
            // then
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
            assertThat(responseEntity.getBody())
                    .extracting(UserResponse::getId, UserResponse::getName, UserResponse::getEmail)
                    .containsExactly(2, "次郎", "yyy@example.com");
        }

        @Test
        void 存在しないレコード() throws JsonProcessingException {
            // when
            ResponseEntity<String> responseEntity = restTemplate.exchange("/api/user/id/99", HttpMethod.GET,
                    new HttpEntity<>(httpHeaders), String.class);
            // then
            Map<String, String> body = mapper.readValue(responseEntity.getBody(), new TypeReference<>() {
            });
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(404);
            assertThat(body.get("message")).isEqualTo("IDが 99 のユーザーは存在しません。");
        }
    }

    @Nested
    @Order(2)
    class Update {
        @Test
        void success() {
            // given
            Map<String, String> userRequestMap = new HashMap<>();
            userRequestMap.put("name", "三郎次郎太郎");
            userRequestMap.put("email", "aaa@example.com");
            // when
            ResponseEntity<Void> responseEntity = restTemplate.exchange("/api/user/3", HttpMethod.POST,
                    new HttpEntity<>(userRequestMap, httpHeaders), Void.class);
            ResponseEntity<UserResponse> actual = restTemplate.exchange("/api/user/id/3", HttpMethod.GET,
                    new HttpEntity<>(httpHeaders), UserResponse.class);
            // then
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(204);
            assertThat(actual.getBody())
                    .extracting(UserResponse::getId, UserResponse::getName, UserResponse::getEmail)
                    .containsExactly(3, "三郎次郎太郎", "aaa@example.com");
        }
    }

    @Nested
    @Order(3)
    class Insert {
        @Test
        void success() {
            // given
            Map<String, String> userRequestMap = new HashMap<>();
            userRequestMap.put("name", "四郎");
            userRequestMap.put("email", "www@example.com");
            // when
            ResponseEntity<Void> responseEntity = restTemplate.exchange("/api/user", HttpMethod.PUT,
                    new HttpEntity<>(userRequestMap, httpHeaders), Void.class);
            ResponseEntity<UserResponse> actual = restTemplate.exchange("/api/user/id/4", HttpMethod.GET,
                    new HttpEntity<>(httpHeaders), UserResponse.class);
            // then
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(201);
            assertThat(responseEntity.getHeaders().get("Location")).hasSize(1);
            assertThat(responseEntity.getHeaders().get("Location").get(0)).isEqualTo("/api/user/4");
            assertThat(actual.getBody())
                    .extracting(UserResponse::getId, UserResponse::getName, UserResponse::getEmail)
                    .containsExactly(4, "四郎", "www@example.com");
        }
    }

    @Nested
    @Order(4)
    class DeleteById {
        @Test
        void success() throws JsonProcessingException {
            // when
            ResponseEntity<Void> responseEntity = restTemplate.exchange("/api/user/2", HttpMethod.DELETE,
                    new HttpEntity<>(httpHeaders), Void.class);
            ResponseEntity<String> actual = restTemplate.exchange("/api/user/id/2", HttpMethod.GET,
                    new HttpEntity<>(httpHeaders), String.class);
            // then
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(204);
            assertThat(actual.getStatusCode().value()).isEqualTo(404);
            Map<String, String> body = mapper.readValue(actual.getBody(), new TypeReference<>() {
            });
            assertThat(actual.getStatusCode().value()).isEqualTo(404);
            assertThat(body.get("message")).isEqualTo("IDが 2 のユーザーは存在しません。");
        }
    }
}

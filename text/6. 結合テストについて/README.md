# 結合テスト について

結合テストは DB を含めて永続化層, service, controller 全てを通して動作を確認する。

JUnit の場合は必要なものは以下の通り。

- Test クラス
- テスト用の SQL マイグレーションファイル
- (任意)テストクラスの前後に実行したいことがあれば`TestExecutionListener`を書く

## テストクラス

```java
package com.example.springboot.it;

// ...

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestExecutionListeners(listeners = { FlywayTestExecutionListener.class }, mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
public class UserApiTest {
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
        void success() {
            // when
            ResponseEntity<Void> responseEntity = restTemplate.exchange("/api/user/2", HttpMethod.DELETE,
                    new HttpEntity<>(httpHeaders), Void.class);
            ResponseEntity<UserResponse> actual = restTemplate.exchange("/api/user/id/2", HttpMethod.GET,
                    new HttpEntity<>(httpHeaders), UserResponse.class);
            // then
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(204);
            assertThat(actual.getStatusCode().value()).isEqualTo(404);
            assertThat(actual.getBody()).isNull();
        }
    }
}
```

新しいアノテーションがいくつか増えたので解説する。
|アノテーション|説明|
|---|---|
|||

# 課題

1.  テキスト中の結合テストを実装せよ。
1.  テキストにない`GET /api/user/1`の結合テストを実装せよ

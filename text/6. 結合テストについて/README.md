# 結合テスト について

結合テストは DB を含めて永続化層, service, controller 全てを通して動作を確認する。

JUnit の場合は必要なものは以下の通り。

- Test クラス
- テスト用の SQL マイグレーションファイル
- (任意)テストクラスの前後に実行したいことがあれば`TestExecutionListener`を書く

---

## テストクラス

```java
package com.example.springboot.it;

// ...

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
|`@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`|ランダムなポートで SpringBoot を立ち上げるという意味。|
|`@TestClassOrder(ClassOrderer.OrderAnnotation.class)`|テストクラス毎に実行順番を指定するために必要。結合テストで CRUD を行うとデータ挿入や変更後に参照をするとデータが変わっているためテストに失敗してしまう。<br>そのため`GET`→`POST`/`PUT`/`DELETE`の順でテストを行う。|
|`@TestInstance(TestInstance.Lifecycle.PER_CLASS)`|テストインスタンスの生成をこのクラスを基準に行うという意味。UserApiTest クラスのコンストラクトが複数回呼ばれないようにするために指定している。|
|`@TestExecutionListeners(listeners = { FlywayTestExecutionListener.class }, mergeMode = MergeMode.MERGE_WITH_DEFAULTS)`|テストクラス（例えば UserApiTest）がコンストラクトされる前後などに実行したい処理を記載した ExecutionListener を紐づける。MERGE_WITH_DEFAULTS はデフォルトの設定に追加するという意味。デフォルトは DI を行う ExecutionListener が紐づいている。|
|`@BeforeAll`|全てのテストを実行する前に一度だけ実行する処理。似たアノテーションに`@AfterAll`, `@BeforeEach`, `@AfterEach`などがある。|
|`@Nested`|テストシナリオをグルーピングする。視認性が上がるのでまとめておくとよい。よくまとめるのはテスト対象毎や正常系・異常系。|

結合テスト特有のクラスがあるので解説する。

| クラス名         | 説明                          |
| ---------------- | ----------------------------- |
| TestRestTemplate | HTTP リクエストを実際に送る。 |

以下のフォーマットで使う。

```java
ResponseEntity<レスポンスクラス> responseEntity = restTemplate.exchange(URLパス, HttpMethod.GET, new HttpEntity<>(httpHeaders), レスポンスクラスの型);
```

以下のように型がない場合は new TypeReference で作れる。たまーに便利。Java11 以降ではジェネリクス(<>のこと)が後ろでは省略できるのでシンプルに書ける。<br>
わざわざ型を作った理由は UserIndexResponse のようにネストしている場合は Jackson がうまくデシリアライズしてくれないため。一応、UserIndexResponse に Request と同様に`@NoArgsConstructor`と`@AllArgsConstructor`をつければデシリアライズできるようになるが、テストのために余計なコードを書きたく無いのでこうしている。

```java
Map<String, List<UserResponse>> body = mapper.readValue(responseEntity.getBody(), new TypeReference<>() {});
```

---

## テスト用の SQL マイグレーションファイル

src/test/resources/db/migration に以下のファイルを追加

```sql
INSERT INTO users (name, email)
VALUES
  (N'三郎', N'zzz@example.com');
```

flyway はデフォルトでは以下のような設定になっている。テストの場合は main と test 両方とも参照される。

| ケース   | 確認するマイグレーションファイルのパス                             |
| -------- | ------------------------------------------------------------------ |
| 通常実行 | src/main/resources/db/migration                                    |
| テスト   | src/main/resources/db/migration<br>src/test/resources/db/migration |

---

## TestExecutionListener

以下では flyway の clean と migrate を実行している。DB の初期化とテスト用のデータの挿入。

```java
package com.example.springboot.listener;

import org.flywaydb.core.Flyway;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class FlywayTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public void beforeTestClass(org.springframework.test.context.TestContext testContext) throws Exception {
        Flyway flyway = testContext.getApplicationContext().getBean(Flyway.class);
        flyway.clean();
        flyway.migrate();
    }
}
```

---

# 課題

1.  テキスト中の結合テストを実装せよ。
1.  テキストにない`GET /api/user/1`の結合テストを実装せよ

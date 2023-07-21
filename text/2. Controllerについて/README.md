# Controller について

Controller の役割はリクエストとレスポンスの変換。

- リクエストを変換して Service に渡す
- Service の出力をレスポンスに変換する

---

## 1. Controller の実装

例えばこんな感じに実装する。

```Java
package com.example.springboot.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sample")
public class SampleController {

    @GetMapping
    public String hello() {
        return "Hello World!";
    }
}
```

関数名を hello にしてるが基本なんでも良い。コントローラに関連するアノテーションについて説明する。

| アノテーション    | 説明                                                                                                                                             |
| ----------------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| `@RestController` | RESTfulAPI のエンドポイントを作成する。                                                                                                          |
| `@RequestMapping` | コントローラのエンドポイントのプレフィックスを指定する。上の例では[http://localhost:8080/sample](http://localhost:8080/sample)でアクセスできる。 |
| `@GetMapping`     | `GET` のエンドポイントを作成する。データの`取得`を行うエンドポイント。                                                                           |
| `@PostMapping`    | `POST` のエンドポイントを作成する。データの`更新`を行うエンドポイント。                                                                          |
| `@PutMapping`     | `PUT` のエンドポイントを作成する。データの`作成`を行うエンドポイント。                                                                           |
| `@DeleteMapping`  | `DELETE` のエンドポイントを作成する。データの`削除`を行うエンドポイント。                                                                        |

HTTP リクエストには大きく分けて 4 つのリクエストがある。それが`GET`/`POST`/`PUT`/`DELETE`である。

---

## 2. リクエストを受け取る方法

4 つの方法がある。

| 方法               | 説明                                                                                                                                                         |
| ------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `パスパラメータ`   | URL パスに入れる。詳細を取得したい場合、ID に紐づくデータを更新、削除したい場合にそのオブジェクトを指定するために使う。`http://localhost:8080/sample/path/1` |
| `クエリパラメータ` | クエリパラメータを指定する。よく検索条件の指定に使う。`http://localhost:8080/sample/query?query=1`                                                           |
| `リクエストボティ` | JSON 形式で渡す。データを作成したい場合や変更したい場合に使用する。                                                                                          |
| `リクエストヘッダ` | キーバリュー形式で渡す。セッション情報や認証情報をを扱う。今回のハンズオンでは触れない。                                                                     |

### 2.1 パスパラメータ

`@PathVariable`を使う。変数名`id`と`/path/{id}`は一致しなくてはならない。

```Java
@GetMapping("/path/{id}")
public String path(@PathVariable Integer id) {
    return "パスパラメータで %d を受け取りました".formatted(id);
}
```

[http://localhost:8080/sample/path/1](http://localhost:8080/sample/path/1)

### 2.2 クエリパラメータ

`@RequestParam`を使う。変数名`id`と`/path/{id}`は一致しなくてはならない。

```Java
@GetMapping("/query")
public String query(@RequestParam Integer id) {
    return "クエリパラメータで %d を受け取りました".formatted(id);
}
```

[hhttp://localhost:8080/sample/query?id=1](http://localhost:8080/sample/query?id=1)

### 2.3 リクエストボディ

`@RequestBody`を使う。リクエストボディを使うメリットはネストした情報を渡すときに便利。

```Java
@PostMapping("/body")
public String body(@RequestBody Integer id) {
    return "リクエストボディで %d を受け取りました".formatted(id);
}
```

リクエストボディの場合はクラスでデシリアライズできる。
クラスでデシリアライズする場合は以下を実装する必要あり。

1. `Setter`
1. `NoArgsConstructor`: 引数なしコンストラクタ
1. `AllArgsConstructor`: 引数全部ありコンストラクタ
1. `Getter`: なくてもデシリアライズできるけど、中身を取り出すために必要。

```Java
package com.example.springboot.web.request;

public class UserRequest {
    private String name;
    private Integer age;

    // NoArgsConstructor
    public UserRequest() {
    }

    // AllArgsConstructor
    public UserRequest(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    // Setter
    public void setName(String value) {
        this.name = value;
    }

    // Setter
    public void setAge(Integer value) {
        this.age = value;
    }

    // Getter
    public String getName() {
        return this.name;
    }

    // Getter
    public Integer getAge() {
        return this.age;
    }
}
```

lombok を入れるとアノテーションで省略できる。

```Java
package com.example.springboot.web.request;

@Data // @Getter + @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String name;
    private Integer age;
}
```

controller は以下になる。

```Java
@PostMapping("/user")
public String user(@RequestBody UserRequest userRequest) {
    return "リクエストボディで 名前:%s, 年齢:%d を受け取りました".formatted(userRequest.getName(), userRequest.getAge());
}
```

### 2.3.1 lombok のインストール方法

pom.xml に以下を追加する。

```xml
<dependencies>
  <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <scope>compile</scope>
  </dependency>
</dependencies>
```

### 2.3.2 動作確認

```shell
$ curl -X POST -H "Content-Type: application/json" -d '{"name" : "佐藤" , "age" : 25}' http://localhost:8080/sample/user

リクエストボディで 名前:佐藤, 年齢:25 を受け取りました
```

---

## 3. レスポンスを返す方法

エンドポイントの関数の返り値が JSON 形式でシリアライズされてレスポンスボティに格納される。

```Java
@GetMapping("/user/{id}")
public UserResponse user(@PathVariable Integer id) {
    return UserResponse.builder().name("伊藤").age(20).build();
}
```

レスポンスは`Getter`だけあれば良い。値の代入には`@AllArgsConstructor`を使うか、`@Builder`を使う。おすすめは後者。

```Java
package com.example.springboot.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private String name;
    private Integer age;
}
```

[http://localhost:8080/sample/user/1](http://localhost:8080/sample/user/1)

---

# 課題

1. パスパラメータで受け取った値を 2 乗して返すエンドポイントを作成せよ。

   [http://localhost:8080/sample/square/2](http://localhost:8080/sample/square/2)にアクセスすると 4 を表示する。
   [http://localhost:8080/sample/square/3](http://localhost:8080/sample/square/3)にアクセスすると 9 を表示する。

2. 身長体重を受け取って BMI を返すエンドポイントを作成せよ。

   ```shell
   $ curl -X POST -H "Content-Type: application/json" -d '{"weight" : "70" , "height" : 1.7}' http://localhost:8080/sample/bmi

   24.221453287197235
   ```

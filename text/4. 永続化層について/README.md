# 永続化層について

データの作成、読み取り、更新、削除 (`CRUD`)を行う。
使うデータベースの種類によって実装が異なる。ビジネスロジック切り離して開発することで変更や再利用が容易なコードになる。

データベースの種類ごとに簡単に実装方法を解説する。

---

# RDB

リレーショナルデータベース（RDB：Relational Database）は、関連するデータをテーブルの集合として格納し、それらのテーブル間のリレーション（関係）に基づいてデータを操作するためのデータベースである。テーブルは行（レコード）と列（フィールド）で構成され、それぞれの行はユニークなキーによって識別される。

特徴

1. **データの構造化**: データは行と列からなるテーブルに保存される。各行は個々のデータレコードを表し、各列はそのレコードの特定の属性（フィールド）を表す。

2. **リレーション（関係）**: テーブル間の関連性を表すことができる。たとえば、顧客テーブルと注文テーブルがある場合、各注文は特定の顧客にリンクできる。これにより、あるテーブルのデータと他のテーブルのデータを結合して複雑なクエリを行うことができる。

3. **データの整合性**: リレーショナルデータベースでは、一貫性と整合性の維持を保証するためのいくつかの制約（一意性、非 Null、外部キー制約など）が設定できる。

4. **SQL（Structured Query Language）**: リレーショナルデータベースは通常、SQL と呼ばれる言語を使用して操作する。SQL を使用すると、データの検索、追加、更新、削除などの操作が行える。

主に使用されるリレーショナルデータベースには、MySQL、PostgreSQL、Oracle Database、Microsoft SQL Server などがある。

今回は OSS の`PostgreSQL`を使って実装を説明する

---

## PostgreSQL の永続化層の実装

1. docker-compose を使って PostgreSQL コンテナを起動

   ```yaml
   services:
     postgres:
       image: postgres:14.2
       ports:
         - "5432:5432"
       environment:
         POSTGRES_USER: sample
         POSTGRES_PASSWORD: sample
         POSTGRES_DB: sample
       healthcheck:
         test: psql -U postgres -d postgres -c 'select 1'
       volumes:
         - ./postgres-data:/var/lib/postgresql/data
   ```

   ```shell
   $ docker-compose up
   ```

1. pom.xml に以下を追加

   ```xml
   <dependencies>
       <!-- 以下の依存を追記する -->
       <dependency>
           <groupId>org.postgresql</groupId>
           <artifactId>postgresql</artifactId>
       </dependency>
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-data-jdbc</artifactId>
       </dependency>
       <dependency>
           <groupId>org.mybatis.spring.boot</groupId>
           <artifactId>mybatis-spring-boot-starter</artifactId>
           <version>3.0.2</version>
       </dependency>
   </dependencies>
   ```

   mybatis は`SQL Mapper`と呼ばれるもので、SQL とその結果をエンティティにマッピングする。

1. application.yaml に接続情報を追加
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/sample
       username: sample
       password: sample
   ```
1. エンティティを追加

   ```java
   package com.example.springboot.persistence.entity;

   //...

   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   @Builder
   public class User {

       private Integer id;
       private String name;
       private String email;
   }
   ```

1. Mapper の実装

   ```java
   package com.example.springboot.persistence.repository;

   // ...

   import com.example.springboot.persistence.entity.User;

   @Mapper
   public interface UserMapper {

       @Select("SELECT * FROM users;")
       List<User> findAll();

       @Select("SELECT * FROM users WHERE id = #{id};")
       User findById(Integer id);

       @Insert("INSERT INTO users(name, email) VALUES(#{name}, #{email});")
       @Options(useGeneratedKeys = true, keyProperty = "id")
       void insert(User user);

       @Update("UPDATE users SET name = #{name}, email = #{email} WHERE id = #{id};")
       void update(User user);

       @Delete("DELETE FROM users WHERE id = #{id};")
       void deleteById(Integer id);
   }
   ```

   SQL Mapper の場合はこのように SQL を書く必要がある。

1. DI を行う

   ```java
   package com.example.springboot.service;

   //...

   import com.example.springboot.persistence.repository.UserMapper;
   import com.example.springboot.persistence.entity.User;

   @Service
   public class UserService {

       @Autowired
       private UserMapper userMapper;

       public List<User> findAll() {
           return userMapper.findAll();
       }

       public User findById(Integer id) {
           return userMapper.findById(id);
       }

       public void insert(User user) {
           userMapper.insert(user);
       }

       public void update(User user) {
           userMapper.update(user);
       }

       public void deleteById(Integer id) {
           userMapper.deleteById(id);
       }
   }
   ```

---

### flyway で migration 管理

さらに初期データを用意してあげる必要がある。今回はマイグレーション管理には`flyway`を使う。

- flyway は実行した SQL のファイル名とそのハッシュ値を管理している。
- flyway は V1.0.0__sample.sql のように`^V\d+(\.\d+)*__.*\.sql$`の形式の SQL ファイルを追跡しバージョン順に実行してくれる。
- まだ実行していないファイルを見つけると実行する。
- 実行済みのバージョンより古いバージョンがあるとエラーになる。
- 過去に実施したファイルのハッシュ値が変わるとエラーになる。

1. pom.xml に以下を追加

   ```xml
   <dependencies>
       <!-- 以下の依存を追記する -->
       <dependency>
           <groupId>org.flywaydb</groupId>
           <artifactId>flyway-core</artifactId>
       </dependency>
   </dependencies>
   ```

2. src/main/resources/db/migration に以下のファイルを追加

   3. V1.0.0\_\_users_schema.sql

   ```sql
   CREATE TABLE users
   (
     id SERIAL PRIMARY KEY,
     name VARCHAR(255) NOT NULL,
     email VARCHAR(255) NOT NULL
   );
   ```

   4. V1.0.1\_\_users_data.sql

   ```sql
   INSERT INTO users (name, email)
   VALUES
     (N'太郎', N'xxx@example.com'),
     (N'次郎', N'yyy@example.com');
   ```

---

# NoSQL

NoSQL データベースは、従来のリレーショナルデータベース（SQL データベース）とは異なるデータストレージと取得のアプローチを採用している。NoSQL の名前は「Not Only SQL」を意味し、これは NoSQL データベースが SQL だけでなく他のデータモデリングやクエリ言語をもサポートしていることを示している。

特徴

1. **スキーマレス**: これらのデータベースはフレキシブルな「スキーマレス」データモデルを採用しているため、データの構造が時間とともに変化する場合や、各レコードが異なる属性を持つ場合でも対応できる。

2. **水平スケーラビリティ**: NoSQL データベースは分散システムで設計されているため、大量のデータやリクエストを処理するためにシステムを水平にスケーリング（システムを追加する）ことが可能である。

3. **多様なデータモデル**: NoSQL データベースはキー・バリューストア、ドキュメントストア、ワイドカラムストア、グラフデータベースなど、さまざまなデータモデルをサポートしている。

一部の代表的な NoSQL データベースには、MongoDB（ドキュメントストア）、Cassandra（ワイドカラムストア）、Redis（キー・バリューストア）、Neo4j（グラフデータベース）などがある。

今回は OSS の`MongoDB`を使って実装を説明する

---

## MongoDB の永続化層の実装

1. docker-compose を使って mongodb コンテナを起動

   ```yaml
   version: "3.1"

   services:
     mongodb:
       image: mongo
       restart: always
       environment:
         MONGO_INITDB_ROOT_USERNAME: sample
         MONGO_INITDB_ROOT_PASSWORD: sample
       ports:
         - 27017:27017
       volumes:
         - ./mongodbdata:/data/db

     mongo-express:
       image: mongo-express
       restart: always
       ports:
         - 8081:8081
       environment:
         ME_CONFIG_MONGODB_ADMINUSERNAME: sample
         ME_CONFIG_MONGODB_ADMINPASSWORD: sample
         ME_CONFIG_MONGODB_SERVER: mongodb
       depends_on:
         - mongodb
   ```

   ```sh
   $ docker-compose up
   ```

1. pom.xml に以下を追加

   ```xml
   <dependencies>
     <!-- 以下の依存を追記する -->
     <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-mongodb</artifactId>
     </dependency>
    ...
   </dependencies>
   ```

1. application.yaml に接続情報を追加
   ```yaml
   spring:
     data:
       mongodb:
         host: localhost
         port: 27017
         database: sample
         username: sample
         password: sample
         authentication-database: admin
   ```
1. エンティティを定義

   ```java
   package com.example.springboot.persistence.entity;

   //...

   @Document
   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   @Builder
   public class Comment {

       @Id
       private String id;
       private String role;
       private String content;
       @CreatedDate
       private LocalDateTime createdAt;
       @LastModifiedDate
       private LocalDateTime updatedAt;
       @Version
       private Long version;
   }
   ```

   アノテーションの説明
   | アノテーション | 説明 |
   | --- | --- |
   |`@Document` | MongoDB のドキュメントへのマッピングを表す。ドキュメントとは RDB でいう行のこと。 |
   |`@Id`| そのフィールドがドキュメントの一意な識別子（つまり、主キー）であることを示す。MongoDB 上では`_id`という名前で保存される。 |

   エンティティは引数なしコンストラクタ、引数全部コンストラクタ、ゲッター、セッターが必須。

1. repository を定義

   ```Java
   package com.example.springboot.persistence.repository;

   import org.springframework.stereotype.Repository;
   import com.example.springboot.persistence.entity.Comment;
   import org.springframework.data.mongodb.repository.MongoRepository;

   @Repository
   public interface CommentRepository extends MongoRepository<Comment, String> {
   }
   ```

   `MongoRepository`インタフェースを継承しているとビルド時に幾つかのメソッドが勝手に実装される。

   - **save(E entity)**: エンティティの保存。Upsert を行う。Upsert とは ID がなければ新規作成。あったら更新。
   - **findById(String id)**: id で検索
   - **existsById(String id)**: 指定された id を持つエンティティが存在するか検索。
   - **findAll()**: 全部取得
   - **count()**: 総数を検索
   - **deleteById(String id)**: 指定された id を持つエンティティを削除
   - **deleteAll()**: 全部削除

   また **findByName(String name)** のようなメソッドを定義してあげると name による検索を実装してくれる。

1. DI を行う

   例えばこんな感じ。

   ```java
   package com.example.springboot.service;

   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.stereotype.Service;

   import com.example.springboot.persistence.entity.Comment;
   import com.example.springboot.persistence.repository.CommentRepository;

   @Service
   public class CommentService {

       @Autowired
       private CommentRepository commentRepository;

       public List<Comment> findAll() {
           return commentRepository.findAll();
       }

       public Comment findById(String id) {
           return commentRepository.findById(id).orElse(null);
       }

       public Long count() {
           return commentRepository.count();
       }

       public Comment save(Comment comment) {
           return commentRepository.save(comment);
       }

       public void deleteById(String id) {
           commentRepository.deleteById(id);
       }

       public void deleteAll() {
           commentRepository.deleteAll();
       }
   }
   ```

1. created_at とか updated_at を自動入力にしたい場合は Auditing を有効化する必要がある。

   ```java
   package com.example.springboot;

   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import org.springframework.data.mongodb.config.EnableMongoAuditing;

   @EnableMongoAuditing
   @SpringBootApplication
   public class Application {

     public static void main(String[] args) {
       SpringApplication.run(Application.class, args);
     }

   }
   ```

# 課題

1. PostgreSQL, mongoDB どちらでも良いのでテキスト中の永続化層を実装せよ
1. Service を実装せよ
1. Controller を実装し外部からアクセスできることを確認せよ
1. HTTP 通信で GET, POST, PUT, DELETE が実行できることを確認せよ

   1. PostgreSQL の場合

      1. GET
         ```shell
         curl http://localhost:8080/api/user
         ```
      1. POST
         ```shell
         curl -X POST -H "Content-Type: application/json" -d '{"name" : "太郎丸" , "email" : "aaa@exmple.com"}' http://localhost:8080/api/user
         ```
      1. PUT
         ```shell
         curl -X PUT -H "Content-Type: application/json" -d '{"name" : "三郎" , "email" : "zzz@exmple.com"}' http://localhost:8080/api/user/1
         ```
      1. DELETE
         ```shell
         curl -X DELETE http://localhost:8080/api/user/2
         ```

   1. MongoDB の場合

      1. GET
         ```shell
         curl http://localhost:8080/api/comment
         ```
      1. PUT

         初期データがないので先に PUT しておく。

         ```shell
         curl -X PUT -H "Content-Type: application/json" -d '{"role" : "user" , "content" : "こんにちは"}' http://localhost:8080/api/comment
         ```

      1. POST

         id 部分は PUT の結果を参考に取得した値に置き換える。

         ```shell
         curl -X POST -H "Content-Type: application/json" -d '{"role" : "user" , "content" : "こんばんは"}' http://localhost:8080/api/comment/64d3009d2bf42014c43da028
         ```

      1. DELETE

         id 部分は PUT の結果を参考に取得した値に置き換える。

         ```shell
         curl -X DELETE http://localhost:8080/api/comment/64d3009d2bf42014c43da028
         ```

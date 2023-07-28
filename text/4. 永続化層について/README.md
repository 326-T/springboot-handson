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
       <dependency>
           <groupId>org.postgresql</groupId>
           <artifactId>postgresql</artifactId>
       </dependency>
       <dependency>
           <groupId>org.mybatis.spring.boot</groupId>
           <artifactId>mybatis-spring-boot-starter</artifactId>
           <version>2.1.4</version>
       </dependency>
   </dependencies>
   ```

   mybatis は`SQL Mapper`と呼ばれるもので、SQL とその結果をエンティティにマッピングする。

1. application.yaml に接続情報を追加
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/test
       username: user
       password: password
   ```
1. エンティティを追加

   ```java
   package com.example.springboot.persistence.entity;

   //...

   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   @Builder
   public class Department {

       private String id;
       private String name;
       private String description;
   }

   ```

1. Mapper の実装

   ```java
   package com.example.springboot.persistence.repository;

   // ...

   import com.example.springboot.persistence.entity.Department;

   @Mapper
   public interface DepartmentMapper {

       @Select("SELECT * FROM department WHERE id = #{id}")
       Department findById(Long id);

       @Insert("INSERT INTO department(name, description) VALUES(#{name}, #{description})")
       Department insert(Department department);

       @Update("UPDATE department SET name = #{name}, description = #{description} WHERE id = #{id}")
       Department update(Department department);

       @Delete("DELETE FROM department WHERE id = #{id}")
       void deleteById(Long id);
   }
   ```

   SQL Mapper の場合はこのように SQL を書く必要がある。対して ORMapper の場合は

1. DI を行う

   ```java
   package com.example.springboot.service;

   //...

   import com.example.springboot.persistence.repository.DepartmentMapper;
   import com.example.springboot.persistence.entity.Department;

   @Service
   public class DepartmentService {

       @Autowired
       private DepartmentMapper departmentMapper;

       public Department findById(Long id) {
           return departmentMapper.findById(id);
       }

       public Department insert(Department department) {
           return departmentMapper.insert(department);
       }

       public Department update(Department department) {
           return departmentMapper.update(department);
       }

       public void deleteById(Long id) {
           departmentMapper.deleteById(id);
       }
   }
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
    ...
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
   package com.example.springboot.persistence.repository;

   //...

   @Document
   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   @Builder
   public class User {

       @Id
       private String id;
       private String name;
       private String email;
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

   import org.springframework.data.mongodb.repository.MongoRepository;

   @Repository
   public interface UserRepository extends MongoRepository<User, String> {
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

   import com.example.springboot.persistence.entity.User;
   import com.example.springboot.persistence.repository.UserRepository;

   @Service
   public class UserService {

       @Autowired
       private UserRepository userRepository;

       public User save(User user) {
           return userRepository.save(user);
       }

       public Boolean existsById(String id) {
           return userRepository.existsById(id);
       }

       public User findById(String id) {
           return userRepository.findById(id).orElse(null);
       }

       public Iterable<User> findAll() {
           return userRepository.findAll();
       }

       public Long count() {
           return userRepository.count();
       }

       public void deleteById(String id) {
           userRepository.deleteById(id);
       }

       public void deleteAll() {
           userRepository.deleteAll();
       }
   }

   ```

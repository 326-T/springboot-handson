# Service について

ビジネスロジックを実装する。

ビジネスロジックとは、企業や組織がその業務を遂行するために必要なルールや手続きをコードで表現したものである。
主にデータの操作やビジネスルールの適用、計算などを行う。

## 1. Service の実装

例えば、こんな感じに実装する。

```java
package com.example.springboot.service;

import org.springframework.stereotype.Service;

@Service
public class SampleService {
    public Integer square(Integer number) {
        return number * number;
    }
}
```

サービスに関連するアノテーションについて説明する。

| アノテーション | 説明                                                                             |
| -------------- | -------------------------------------------------------------------------------- |
| `@Service`     | `@Component`と同じ。ビジネスロジックであることを明示するために`@Service`をつける |

サービスを書いたら`DI`する。

```java
package com.example.springboot.web.controller;
//...

@RestController
@RequestMapping("/sample")
public class SampleController {

    @Autowired
    private SampleService sampleService;
    //...

    @GetMapping("/square/{number}")
    public Integer square(@PathVariable Integer number) {
        return sampleService.square(number);
    }
}
```

# 課題

1. 身長体重を受け取って BMI を返すエンドポイントのビジネスロジックを`Service`に移行せよ。

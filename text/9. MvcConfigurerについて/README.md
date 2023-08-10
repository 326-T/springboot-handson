# WebMvcConfigurer

SpringMVC の設定をカスタムする際に編集する。<br>
主に設定できる項目は

1. **静的リソースのハンドリング**:

   - `addResourceHandlers`メソッドをオーバーライドして、静的リソースの取り扱いをカスタマイズできる。

1. **インターセプタの追加**:

   - `addInterceptors`メソッドをオーバーライドして、特定の URL パターンに対しての前後処理を行うインターセプタを追加できる。

1. **CORS 設定**:
   - `addCorsMappings`メソッドをオーバーライドして、CORS の設定をカスタマイズできる。

---

## WebMvcConfigurer の実装

例えば CORS 設定を実装してみる。

```java
package com.example.springboot.config;

// ...

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // ...

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("*")
                .allowedOriginPatterns("http://localhost:*")
                .allowCredentials(true)
                .allowedHeaders("*")
        ;
    }
    // ...
}
```

---

# 課題

テキストを参考に CORS 設定をせよ

# 回退到 Java 8 配置（备选方案）

## 如果需要回退到 Java 8

如果您暂时不想安装 JDK 17，可以回退配置：

### pom.xml 修改

```xml
<!-- 修改 Spring Boot 版本 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
</parent>

<!-- 修改 Java 版本 -->
<properties>
    <java.version>1.8</java.version>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
    ...
</properties>

<!-- MySQL Connector 改回旧版本 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

### 代码修改

1. 所有 `jakarta.persistence.*` 改回 `javax.persistence.*`
2. 删除 Java 17 新特性（Text Blocks, Switch Expressions）
3. 使用传统语法

**但不推荐这样做**，因为 Java 17 有很多优势！



# Spring Boot 后端服务

这是一个基于 Spring Boot 3.2.x 和 Java 21 的后端服务。

## 技术栈

- Spring Boot 3.2.3
- Java 21
- Spring Data JPA
- PostgreSQL
- RESTful API

## 系统要求

- Java 21 或更高版本
- Maven 3.6 或更高版本
- PostgreSQL 12 或更高版本

## 功能

- 用户管理 RESTful API（增删改查）

## 数据库配置

在 `application.properties` 文件中配置以下内容：

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/demo
spring.datasource.username=postgres
spring.datasource.password=postgres
```

## 启动应用

1. 确保 PostgreSQL 数据库已启动并创建了名为 "demo" 的数据库
2. 执行下面的命令构建并运行应用

```bash
mvn clean install
mvn spring-boot:run
```

应用将在 http://localhost:8080 上启动。

## API 端点

- GET /api/users - 获取所有用户
- GET /api/users/{id} - 通过 ID 获取用户
- POST /api/users - 创建新用户
- PUT /api/users/{id} - 更新用户
- DELETE /api/users/{id} - 删除用户

## 前端集成

可以通过 Vue.js 前端调用这些 API 端点。
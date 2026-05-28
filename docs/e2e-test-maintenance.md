# E2E 测试维护文档

**最后更新**: 2026-05-29
**维护者**: Orion

---

## 1. 概述

本文档定义 MiniMall 项目 E2E 测试套件的维护规范和最佳实践，确保测试稳定可靠。

## 2. 测试架构

### 2.1 核心组件

| 组件 | 位置 | 职责 |
|------|------|------|
| E2ETestBase | `src/test/java/com/minimall/e2e/E2ETestBase.java` | 提供统一的认证流程和请求辅助 |
| E2ETestConfig | `src/test/java/com/minimall/config/E2ETestConfig.java` | 测试数据初始化和 beans |
| AuthFlowE2ETest | `src/test/java/com/minimall/e2e/AuthFlowE2ETest.java` | 认证流程测试 |
| OrderFlowE2ETest | `src/test/java/com/minimall/e2e/OrderFlowE2ETest.java` | 订单流程测试 |
| PaymentFlowE2ETest | `src/test/java/com/minimall/e2e/PaymentFlowE2ETest.java` | 支付流程测试 |

### 2.2 测试 profile

E2E 测试使用独立的 `test` profile：

```yaml
# src/test/resources/application-test.yml
spring:
  application:
    name: minimall-test
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
```

### 2.3 配置隔离

| 配置项 | 主配置 | 测试配置 |
|--------|--------|----------|
| 数据库 | PostgreSQL (prod) | H2 In-Memory (test) |
| JWT Secret | 生产环境密钥 | `TEST_JWT_SECRET_KEY_...` |
| WeChat Pay | 生产配置 | Mock 配置 |

## 3. 编写新测试

### 3.1 基本模板

```java
package com.minimall.e2e;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NewFlowE2ETest extends E2ETestBase {

    @Test
    void testScenario_specificCondition_expectedResult() throws Exception {
        // 1. 获取认证 token
        String token = loginAndGetToken();

        // 2. 准备请求数据
        var requestBody = new RequestClass(...);

        // 3. 执行请求并验证
        authenticatedPost("/api/your-endpoint")
            .withToken(token)
            .withBody(requestBody)
            .execute(status().isOk());
    }
}
```

### 3.2 测试数据要求

**必须**在 `E2ETestConfig` 中注册的数据：
1. **User**：测试用户（用于登录）
2. **Product**：商品数据（用于订单等场景）
3. **其他业务数据**：根据需要添加

**注意**：每个测试应该能够独立运行，不依赖其他测试的状态。

### 3.3 认证注意事项

- 所有非公开端点的测试**必须**使用 `loginAndGetToken()` 获取 token
- token 应在请求头中传递：`Authorization: Bearer <token>`
- 使用 `authenticatedGet()` / `authenticatedPost()` 辅助方法更便捷

## 4. 运行测试

### 4.1 本地运行

```bash
# 运行所有 E2E 测试
mvn test -Dtest="*E2E*" -Dspring.profiles.active=test

# 运行单个测试类
mvn test -Dtest="AuthFlowE2ETest" -Dspring.profiles.active=test

# 运行单个测试方法
mvn test -Dtest="AuthFlowE2ETest#loginFlow_withValidCredentials_returnsSuccess" -Dspring.profiles.active=test
```

### 4.2 CI 运行

E2E 测试在 CI 的 `build` 阶段自动运行：

```yaml
# .github/workflows/ci.yml
- name: Run E2E tests
  run: mvn test -Dtest="*E2E*" -Dspring.profiles.active=test
```

## 5. 故障排查

### 5.1 常见错误

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| `403 Forbidden` | 未提供认证 token | 使用 `loginAndGetToken()` 并添加 Authorization 头 |
| `500 Internal Server Error` | 测试数据缺失 | 检查 `E2ETestConfig` 数据初始化 |
| `ApplicationContext` 启动失败 | 配置绑定错误 | 检查 `@ActiveProfiles("test")` 是否正确 |

### 5.2 调试技巧

```java
// 在测试中添加日志输出
System.out.println("Token: " + token);
System.out.println("Response: " + result.getResponse().getContentAsString());

// 禁用 Spring Security 滤镜进行调试
// 在 E2ETestConfig 中添加
@Bean
@Primary
public SecurityFilterChain debugFilterChain(HttpSecurity http) {
    http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
}
```

## 6. 维护检查清单

在提交 PR 前，请确认：

- [ ] 所有 E2E 测试可以通过 `mvn test -Dtest="*E2E*"`
- [ ] 测试使用了 `@ActiveProfiles("test")`
- [ ] 测试数据已在 `E2ETestConfig` 中注册
- [ ] 没有硬编码的测试数据（如固定的用户 ID）
- [ ] 测试之间相互隔离，不共享状态

## 7. 相关文档

- [E2E 测试健康检查](./e2e-test-health-check.md)
- [Sprint #228 E2E 稳定性分析](./sprints/sprint-228-e2e-stability-analysis.md)
- [CI 配置](../.github/workflows/ci.yml)

---

**更新记录**

| 版本 | 日期 | 修改内容 | 执行者 |
|------|------|----------|--------|
| 1.0 | 2026-05-29 | 初始版本 | Orion |

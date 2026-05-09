# MiniMall 日志规范 (ALERTING.md)

## 1. 规范目的

本规范定义了 MiniMall 项目中的日志记录标准，确保日志能够有效支持监控、调试和故障排查。

## 2. 日志级别定义

| 级别 | 使用场景 | 示例 |
|------|---------|------|
| `error` | 未预期的异常、系统级失败、需要立即处理的错误 | 外部 API 调用失败、签名验证失败、关键回调处理异常 |
| `warn` | 可恢复的问题、无效输入、业务流程中的非关键失败 | 用户未订阅消息、支付状态失败、参数校验失败 |
| `info` | 重要的业务事件、状态转换、系统里程碑 | 订单创建、支付成功、用户登录 |
| `debug` | 调试信息、缓存命中、频繁操作的记录 | Token 刷新、缓存命中、循环中的状态 |

## 3. log.error 使用准则

### 3.1 合法场景

```java
// ✅ 外部 API 调用失败
try {
    // WeChat Pay API call
} catch (Exception e) {
    log.error("Failed to sign JSAPI request", e);
}

// ✅ 签名/安全验证失败
} catch (Exception e) {
    log.error("Callback verification failed", e);
}

// ✅ 关键回调处理异常
} catch (Exception e) {
    log.error("Failed to process callback", e);
}
```

### 3.2 禁止场景

```java
// ❌ 不要在消息中使用 "error" 关键字
log.error("Failed to sign JSAPI request: {}", e.getMessage());

// ❌ 不要只记录异常消息，丢失堆栈信息
log.error("Failed to process: {}", e.getMessage());

// ❌ 不要用于业务逻辑流程
log.error("User has not subscribed to messages");
// 应该使用 log.warn
```

### 3.3 正确模式

```java
// ✅ 描述失败的操作，附加完整异常
log.error("Failed to sign JSAPI request", e);

// ✅ 包含上下文信息
log.error("Callback verification failed", e);
```

## 4. 告警规则配置

### 4.1 error 日志告警

当系统产生 `log.error` 级别的日志时，应触发告警：

```yaml
# alertmanager-rules.yml
- alert: MiniMallErrorLog
  expr: |
    rate(logback_events_total{level="error", application="minimall"}[5m]) > 0
  for: 1m
  labels:
    severity: critical
  annotations:
    summary: "MiniMall 产生错误日志"
    description: "系统产生 error 级别日志: {{ $value }}"
```

### 4.2 高错误率告警

```yaml
- alert: MiniMallHighErrorRate
  expr: |
    (
      sum(rate(http_server_requests_seconds_count{application="minimall", status=~"5.."}[5m]))
      /
      sum(rate(http_server_requests_seconds_count{application="minimall"}[5m]))
    ) > 0.01
  for: 5m
  labels:
    severity: critical
```

## 5. Checkstyle 规则

项目配置了 checkstyle.xml 来自动检查日志规范：

```xml
<!-- 检查：不要只记录 getMessage() -->
<module name="RegexpSingleline">
  <property name="format" value="log\.error\([^,]+,\s*(?:e\.getMessage\(\)|getMessage\(\))\)"/>
  <property name="message" value="Use 'log.error(&quot;msg&quot;, ex)' to log full exception"/>
</module>

<!-- 检查：不要在消息中使用 "error" -->
<module name="RegexpSingleline">
  <property name="format" value="log\.error\(&quot;[^&quot;]*\berror\b[^&quot;]*&quot;"/>
  <property name="message" value="Error log should describe what failed, not contain the word 'error'"/>
</module>
```

## 6. 修改记录

| 日期 | 修改内容 | 修改人 |
|------|---------|--------|
| 2026-05-09 | 初始化日志规范文档，修复 PayService 和 WeChatSubscribeService 中的日志问题 | 后端架构师 |

## 7. 相关文件

- `checkstyle.xml` - 日志规范检查配置
- `alertmanager-rules.yml` - 告警规则配置
- `PayService.java` - 支付服务日志示例
- `WeChatSubscribeService.java` - 微信订阅服务日志示例
# Phase 21: 生产环境监控告警机制

## 一、监控架构

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Grafana   │────▶│ Prometheus  │◀────│   Backend   │
│   :3000     │     │   :9090     │     │   :8080     │
└─────────────┘     └─────────────┘     └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ Alertmanager│
                    │   :9093     │
                    └─────────────┘
```

## 二、监控服务访问

| 服务 | 地址 | 默认账号 |
|------|------|---------|
| Grafana | http://localhost:3000 | admin / admin123 |
| Prometheus | http://localhost:9090 | - |
| Alertmanager | http://localhost:9093 | - |

## 三、监控指标

### 3.1 基础设施监控

| 指标 | 说明 | 告警阈值 |
|------|------|----------|
| `up{job="minimall-backend"}` | 服务存活 | = 0 持续 1 分钟 |
| `jvm_memory_used_bytes` | JVM 堆内存使用 | > 90% 持续 10 分钟 |
| `hikaricp_connections_active` | 数据库连接池活跃 | > 90% 持续 5 分钟 |

### 3.2 应用监控

| 指标 | 说明 | 告警阈值 |
|------|------|----------|
| `http_server_requests_seconds_count{status=~"5.."}` | HTTP 5xx 错误率 | > 1% 持续 5 分钟 |
| `http_server_requests_seconds_bucket` | API 响应时间 P99 | > 2s 持续 5 分钟 |
| `minimall_payment_failure` | 支付失败数 | 失败率 > 5% 持续 5 分钟 |

### 3.3 业务指标

| 指标 | 说明 | 来源 |
|------|------|------|
| `minimall.orders.created` | 订单创建数 | MetricsConfig |
| `minimall.orders.paid` | 订单支付成功数 | MetricsConfig |
| `minimall.payment.success` | 支付成功数 | MetricsConfig |
| `minimall.payment.failure` | 支付失败数 | MetricsConfig |

## 四、端点说明

### 4.1 Actuator 端点

后端暴露以下 Actuator 端点：

| 端点 | 地址 | 说明 |
|------|------|------|
| 健康检查 | `GET /actuator/health` | 服务健康状态 |
| Prometheus 指标 | `GET /actuator/prometheus` | Prometheus 格式指标 |
| JVM 信息 | `GET /actuator/metrics/jvm.*` | JVM 相关指标 |
| HTTP 指标 | `GET /actuator/metrics/http.server.requests` | HTTP 请求指标 |

### 4.2 验证命令

```bash
# 检查健康状态
curl http://localhost:8080/actuator/health

# 检查 Prometheus 指标
curl http://localhost:8080/actuator/prometheus | grep -E "minimall|http_server|jvm_memory"

# 检查 Prometheus 目标状态
curl http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | {job: .labels.job, health: .health}'

# 检查告警规则
curl http://localhost:9090/api/v1/rules | jq '.data.groups[].rules[] | select(.type=="alerting") | {name: .name, state: .state}'
```

## 五、告警规则

| 告警名称 | 触发条件 | 严重级别 |
|----------|----------|----------|
| MiniMallServiceDown | 服务不可用 > 1 分钟 | critical |
| MiniMallHighErrorRate | HTTP 5xx 错误率 > 1% | critical |
| MiniMallHighResponseTime | P99 响应时间 > 2 秒 | warning |
| MiniMallHikariCPConnectionsExhausted | 连接池使用 > 90% | warning |
| MiniMallHighPaymentFailureRate | 支付失败率 > 5% | critical |
| MiniMallJVMHeapUsageHigh | JVM 堆内存 > 90% | warning |
| MiniMallAPITimeout | API P99 > 5 秒 | critical |

## 六、Grafana 面板

Grafana 面板包含以下监控视图：

1. **服务概览** - CPU、内存、JVM 状态
2. **HTTP 请求** - 请求率、响应时间、错误率
3. **数据库连接池** - 活跃连接、空闲连接、最大连接
4. **支付监控** - 成功率、失败率、处理时间
5. **业务指标** - 订单创建、支付、转化率

## 七、故障排查

### 7.1 Prometheus 无法抓取指标

```bash
# 检查 backend 是否运行
docker-compose ps backend

# 检查端口是否通
curl http://localhost:8080/actuator/prometheus

# 检查 prometheus 日志
docker-compose logs prometheus | tail -50
```

### 7.2 Grafana 无数据

1. 检查 Prometheus 目标状态：`http://localhost:9090/targets`
2. 确认 `minimall-backend` 目标为 UP
3. 检查时间范围是否正确

### 7.3 告警未触发

1. 检查 Alertmanager 是否运行：`docker-compose ps alertmanager`
2. 检查告警规则是否加载：`http://localhost:9090/rules`
3. 查看 Alertmanager 日志：`docker-compose logs alertmanager`

## 八、相关文件

- `docs/monitoring/prometheus.yml` - Prometheus 配置
- `docs/monitoring/alertmanager-rules.yml` - 告警规则
- `docs/monitoring/grafana-dashboard.json` - Grafana 面板
- `src/main/java/com/minimall/config/MetricsConfig.java` - 自定义指标
- `src/main/resources/application.properties` - Actuator 配置

## 九、更新日志

- **2026-05-05**: 初始文档创建，配置 Spring Boot Actuator
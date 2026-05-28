# E2E Test Health Check

## 概述

本文档定义 MiniMall 项目的端到端测试健康检查规范，确保 E2E 测试套件稳定可靠。

## 检查项目

### 1. 测试环境

| 检查项 | 标准 | 状态 |
|--------|------|------|
| 数据库连接 | 可以成功连接测试数据库 | 必需 |
| 服务启动 | 应用能在测试端口启动 | 必需 |
| 外部依赖 | 所有 Mock 服务正常运行 | 必需 |

### 2. 关键测试覆盖

| 测试场景 | 描述 | 通过标准 |
|----------|------|----------|
| 用户登录流程 | 验证登录功能正常 | 返回 200 + 有效 session |
| 核心业务流程 | 验证主要业务路径 | 端到端无错误 |
| 异常处理 | 验证错误情况处理 | 返回合适的状态码 |

## 执行频率

- 每次 PR 合并前必须运行完整 E2E 测试套件
- 每日定时执行健康检查

## 相关文档

- [CI 配置](../.github/workflows/ci.yml)
- [交付验证](../docs/delivery-verification.md)

## 更新记录

| 日期 | 更新内容 | 操作者 |
|------|---------|--------|
| 2026-05-29 | 修复 E2E 测试基础设施问题：添加 TestMetricsConfig、修复 WeChatPayConfig setAppId、添加 RSAAutoCertificateConfig MockBean | Orion |
| 2026-05-25 | 初始创建，修复 MIN-3470 虚假交付 | Orion |

## 问题诊断

### 常见错误

**ApplicationContext 加载失败**
- 检查 `application.properties` 中的 `customer-service.auto-reply.rules` 配置
- 确保 `@Import(TestMetricsConfig.class)` 包含在所有 E2E 测试类中
- 使用 `@MockBean RSAAutoCertificateConfig` 避免微信支付 API 调用

**MeterRegistry Bean 不存在**
- 添加 `TestMetricsConfig` 配置类提供 `SimpleMeterRegistry`
- 或在测试配置中禁用 metrics

**PasswordEncoder Bean 不存在**
- 在 `TestMetricsConfig` 中添加 `BCryptPasswordEncoder` Bean

**微信支付 API 401 错误**
- 使用 `@MockBean RSAAutoCertificateConfig` 模拟微信支付配置
- 不要尝试连接真实的微信支付 API
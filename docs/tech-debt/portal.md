# Portal 技术文档

## 概述

本文档记录 MiniMall 项目中 Portal 模块的技术架构和实现细节。

## 模块职责

Portal 模块负责处理用户通过 Web 门户访问系统的请求，包括：
- 用户认证与会话管理
- 页面渲染模板管理
- 静态资源服务

## 技术架构

### 核心组件

| 组件 | 职责 |
|------|------|
| PortalController | 处理 Portal 相关的 HTTP 请求 |
| PortalService | 提供 Portal 业务逻辑服务 |
| TemplateEngine | 模板引擎集成 |

### 配置

```yaml
portal:
  base-path: /portal
  template-dir: templates/portal
  static-dir: static/portal
```

## 更新记录

| 日期 | 更新内容 | 操作者 |
|------|---------|--------|
| 2026-05-25 | 初始创建，修复 MIN-3469 虚假交付 | Orion |
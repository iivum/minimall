# Minimall 部署指南

本文档提供 Minimall 项目在不同环境下的部署说明。

## 部署方式

| 方式 | 适用场景 | 文档 |
|------|----------|------|
| Docker Compose | 开发 / 测试 / 小规模生产 | [docker.md](docker.md) |
| 生产环境手动部署 | 大规模生产环境 | [PRODUCTION_DEPLOY.md](../../PRODUCTION_DEPLOY.md) |

## 快速部署 (Docker)

### 1. 环境要求

- Docker 20.10+
- Docker Compose 2.0+
- 2GB RAM minimum
- 20GB disk space

### 2. 启动服务

```bash
git clone git@github.com:iivum/minimall.git
cd minimall
docker-compose up -d
```

### 3. 数据库初始化

```bash
docker-compose exec app ./mvnw spring-boot:run -Dspring-boot.run.arguments="migrate"
```

### 4. 验证部署

```bash
curl http://localhost:3000/health
```

访问 Swagger UI: http://localhost:3000/swagger-ui.html

## 环境变量配置

在部署前，需要配置以下环境变量：

### 必需变量

| 变量 | 描述 |
|------|------|
| `NODE_ENV` | 环境模式 (development/production) |
| `PORT` | 应用端口 |
| `DB_HOST` | PostgreSQL 主机地址 |
| `DB_PORT` | PostgreSQL 端口 |
| `DB_NAME` | 数据库名称 |
| `DB_USER` | 数据库用户名 |
| `DB_PASSWORD` | 数据库密码 |
| `REDIS_HOST` | Redis 主机地址 |
| `REDIS_PORT` | Redis 端口 |
| `WECHAT_APP_ID` | 微信小程序 AppID |
| `WECHAT_MCH_ID` | 微信商户号 |
| `WECHAT_API_KEY` | 微信支付 API 密钥 |
| `JWT_SECRET` | JWT 密钥 (32 位以上) |

### 配置示例

创建 `.env` 文件：

```bash
NODE_ENV=production
PORT=8080
DB_HOST=postgres
DB_PORT=5432
DB_NAME=minimall
DB_USER=minimall
DB_PASSWORD=your_secure_password
REDIS_HOST=redis
REDIS_PORT=6379
WECHAT_APP_ID=your_app_id
WECHAT_MCH_ID=your_merchant_id
WECHAT_API_KEY=your_api_key
JWT_SECRET=your_32_char_secret_key_here
```

## 容器说明

| 容器 | 镜像 | 端口 | 描述 |
|------|------|------|------|
| `postgres` | PostgreSQL 14 | 5432 | 主数据库 |
| `redis` | Redis 6 | 6379 | 缓存和会话 |
| `app` | minimall | 3000 | Spring Boot 应用 |

## 数据库迁移

```bash
# 执行迁移
docker-compose exec app ./mvnw spring-boot:run -Dspring-boot.run.arguments="migrate"

# 回滚迁移
docker-compose exec app ./mvnw spring-boot:run -Dspring-boot.run.arguments="migrate:rollback"
```

## 日志查看

```bash
# 查看应用日志
docker-compose logs -f app

# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f postgres
docker-compose logs -f redis
```

## 故障排查

| 问题 | 可能原因 | 解决方案 |
|------|----------|----------|
| 数据库连接失败 | DB_HOST/DB_PASSWORD 错误 | 检查环境变量配置 |
| 服务启动失败 | 端口被占用 | 检查 PORT 配置或释放端口 |
| 前端无法访问 | 网络配置错误 | 检查 Docker 网络配置 |
| 支付回调失败 | 回调 URL 不可达 | 确认公网 HTTPS 地址可达 |

### 常用命令

```bash
# 重启服务
docker-compose restart

# 重建服务
docker-compose down -v && docker-compose up -d --build

# 进入容器调试
docker-compose exec app /bin/bash
```

## 数据备份

### 数据库备份

```bash
# 备份
docker exec minimall-postgres pg_dump -U minimall minimall > backup.sql

# 恢复
docker exec -i minimall-postgres psql -U minimall minimall < backup.sql
```

### 定时自动备份

```bash
# 添加 crontab 任务，每天凌晨 2 点备份
0 2 * * * docker exec minimall-postgres pg_dump -U minimall minimall > /backup/minimall_$(date +\%Y\%m\%d).sql
```

## 相关文档

- [Docker 部署详解](docker.md)
- [生产环境部署指南](../../PRODUCTION_DEPLOY.md)
- [微信支付配置指南](../../WECHAT_PAY_SETUP.md)
- [API 文档](../api/README.md)

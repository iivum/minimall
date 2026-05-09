# MiniMall 微信小程序商城

MVP 微信小程序电商平台，提供商品管理、订单处理、会员系统和微信支付集成。

## 项目架构

```
minimall/
├── src/main/java/com/minimall/
│   ├── controller/     # REST API 控制器
│   ├── service/        # 业务逻辑层
│   ├── repository/      # 数据访问层 (JPA)
│   ├── model/          # 实体类
│   ├── dto/            # 数据传输对象
│   ├── config/         # 配置类
│   ├── exception/      # 自定义异常
│   └── util/           # 工具类
├── src/main/resources/
│   ├── application.yml       # 应用配置
│   └── db/migration/         # 数据库迁移
├── src/test/java/            # 单元测试
├── docs/
│   ├── api/                  # API 文档
│   ├── deployment/           # 部署指南
│   ├── user-guide/           # 用户指南
│   └── monitoring/          # 监控告警
├── checkstyle.xml            # 代码风格检查配置
├── docker-compose.yml        # Docker 编排
└── pom.xml                   # Maven 依赖
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.5 (Java 17) |
| 数据库 | PostgreSQL + H2 (开发) |
| 安全 | Spring Security + JWT |
| API 文档 | SpringDoc OpenAPI 3 (Swagger) |
| 微信支付 | WeChat Pay API v3 |
| 构建工具 | Maven |
| 容器化 | Docker Compose |

## 快速开始

### 环境要求

- Java 17+
- Maven 3.8+
- PostgreSQL 15+ (生产)
- Docker & Docker Compose (容器部署)

### 本地开发

```bash
# 克隆项目
git clone https://github.com/iivum/minimall.git
cd minimall

# 配置环境变量
cp .env.example .env
# 编辑 .env 填入必要的配置

# 启动数据库 (Docker)
docker-compose up -d postgres

# 运行应用
./mvnw spring-boot:run

# 访问 API 文档
open http://localhost:8080/swagger-ui.html
```

### Docker 部署

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend

# 健康检查
curl http://localhost:8080/health
```

## 主要功能

### 商品管理
- 商品列表 (分页、搜索、排序)
- 商品详情
- 库存管理

### 订单系统
- 创建订单
- 订单状态管理
- 订单支付 (微信支付)

### 会员系统
- 会员等级
- 积分账户
- 会员权益 (折扣、兑换)
- 优惠券

### 微信支付
- 统一下单
- 支付回调
- 退款处理

## API 文档

完整 API 文档请参考 [docs/api/README.md](docs/api/README.md)

### 基础信息

- **Base URL**: `http://localhost:8080/api`
- **认证**: Bearer Token (JWT) 在 Authorization header 中传递
- **文档**: Swagger UI at `/swagger-ui.html`

### 认证接口

```bash
# 登录
POST /api/auth/login
{"openid": "微信openid"}

# 注册
POST /api/auth/register
{"openid": "...", "nickname": "...", "phone": "...", "avatarUrl": "..."}
```

### 商品接口

```bash
# 获取商品列表 (分页)
GET /api/products?page=0&size=10&search=手机&sort=price-asc

# 获取所有商品
GET /api/products/all

# 获取商品详情
GET /api/products/{id}
```

### 订单接口

```bash
# 创建订单
POST /api/orders
{"userId": "...", "items": [{"productId": "...", "quantity": 1}]}

# 获取用户订单
GET /api/orders/user/{userId}

# 支付订单
POST /api/pay/create/{orderId}?openid=...
```

## 配置说明

### 环境变量

| 变量 | 描述 | 必填 |
|------|------|------|
| `POSTGRES_HOST` | PostgreSQL 主机 | 是 |
| `POSTGRES_PORT` | PostgreSQL 端口 | 是 |
| `POSTGRES_DB` | 数据库名 | 是 |
| `POSTGRES_USER` | 数据库用户 | 是 |
| `POSTGRES_PASSWORD` | 数据库密码 | 是 |
| `WECHATPAY_MCHID` | 微信商户号 | 是 |
| `WECHATPAY_SERIAL_NO` | 证书序列号 | 是 |
| `WECHATPAY_API_V3_KEY` | APIv3 密钥 | 是 |
| `JWT_SECRET` | JWT 密钥 (32+ 字符) | 是 |

详见 [PRODUCTION_DEPLOY.md](PRODUCTION_DEPLOY.md)

## 数据库

### 开发模式 (H2)

开发环境使用 H2 内存数据库，无需额外配置。

### 生产模式 (PostgreSQL)

```bash
# 创建数据库
CREATE DATABASE minimall;

# 运行迁移 (JPA 自动创建)
# 确保 spring.jpa.hibernate.ddl-auto=update
```

## 测试

```bash
# 运行单元测试
./mvnw test

# 生成覆盖率报告
./mvnw test jacoco:report
```

## 代码规范

- 使用 Google Java Style Guide
- 运行 Checkstyle: `./mvnw checkstyle:check`
- 格式化代码: `./mvnw fmt:format`
- 遵循 [code-merge-checklist.md](code-merge-checklist.md) 进行代码合并验证
- 日志级别规范：见 checkstyle.xml 注释

## 部署

详细部署指南请参考:
- [PRODUCTION_DEPLOY.md](PRODUCTION_DEPLOY.md) - 生产环境部署
- [docs/deployment/docker.md](docs/deployment/docker.md) - Docker 部署

## 监控

- 健康检查: `GET /health`
- API 文档: `GET /swagger-ui.html`
- OpenAPI JSON: `GET /v3/api-docs`

## 分支策略

- `main` - 生产分支
- `agent/*` - Agent 工作分支

## 许可证

MIT License - 详见 [LICENSE](LICENSE)

## 版本历史

详见 [CHANGELOG.md](CHANGELOG.md)

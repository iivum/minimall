# Minimall

MVP 微信小程序商城，基于 Spring Boot 的后端 API 服务。

## 项目架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        WeChat Mini Program                       │
│                         (miniprogram/)                           │
└─────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼ HTTP/HTTPS
┌─────────────────────────────────────────────────────────────────┐
│                      Spring Boot Backend                          │
│                         (src/main/)                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │  REST API   │  │   WebSocket │  │  Security   │              │
│  │  Controller │  │   Handler  │  │    (JWT)    │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │    JPA     │  │   WeChat    │  │  Validator  │              │
│  │  Repository│  │    Pay      │  │             │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└─────────────────────────────────────────────────────────────────┘
          │                    │                   │
          ▼                    ▼                   ▼
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│ PostgreSQL  │      │   Redis     │      │   Docker    │
│  Database   │      │   Cache     │      │   Compose   │
└─────────────┘      └─────────────┘      └─────────────┘
```

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| Java 版本 | Java | 17 |
| 数据库 | PostgreSQL | - |
| 缓存 | Redis | - |
| 安全 | Spring Security + JWT | - |
| API 文档 | SpringDoc OpenAPI | 2.5.0 |
| 微信支付 | WeChat Pay API v3 | 0.2.17 |
| 构建工具 | Maven | - |
| 容器化 | Docker Compose | - |

## 快速开始

### 环境要求

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Redis 6+

### 1. 克隆项目

```bash
git clone git@github.com:iivum/minimall.git
cd minimall
```

### 2. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 文件配置数据库等参数
```

### 3. 启动数据库服务

```bash
docker-compose up -d postgres redis
```

### 4. 启动应用

```bash
./mvnw spring-boot:run
```

### 5. 验证服务

```bash
curl http://localhost:8080/health
```

服务启动后，访问 API 文档：
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 目录结构

```
minimall/
├── src/main/java/com/minimall/    # Java 源码
│   ├── controller/                 # REST 控制器
│   ├── service/                   # 业务逻辑
│   ├── repository/                # 数据访问层
│   ├── model/                     # 实体类
│   ├── config/                    # 配置类
│   └── security/                  # 安全相关
├── src/main/resources/            # 资源文件
├── src/test/                      # 测试代码
├── miniprogram/                    # 微信小程序前端
├── docs/                           # 文档
│   ├── api/                        # API 文档
│   ├── deployment/                # 部署指南
│   └── user-guide/                 # 用户指南
├── docker-compose.yml              # Docker 编排
├── pom.xml                         # Maven 配置
└── README.md
```

## 功能模块

### 1. 认证 (Authentication)
- 微信登录
- JWT Token 管理
- 刷新令牌

### 2. 商品管理 (Product)
- 商品列表
- 商品详情
- 库存管理

### 3. 订单管理 (Order)
- 创建订单
- 订单列表
- 订单状态流转

### 4. 支付 (Payment)
- 微信支付集成
- 支付回调处理
- 退款功能

### 5. 会员 (Member)
- 会员信息
- 积分管理
- 会员等级

## 部署说明

### Docker 部署

详见 [docs/deployment/docker.md](docs/deployment/docker.md)

```bash
# 快速启动
docker-compose up -d

# 数据库迁移
docker-compose exec app ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring-boot.run.arguments=migrate"

# 健康检查
curl http://localhost:3000/health
```

### 生产环境部署

详见 [PRODUCTION_DEPLOY.md](PRODUCTION_DEPLOY.md)

关键步骤：
1. 配置生产环境 `.env` 文件
2. 设置微信支付商户号和 API 密钥
3. 配置 HTTPS 和域名
4. 设置监控和告警
5. 配置数据备份策略

## API 文档

完整的 API 文档请参考 [docs/api/README.md](docs/api/README.md)。

基础路径：`/api`

| 端点 | 方法 | 描述 |
|------|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/refresh` | POST | 刷新 Token |
| `/api/products` | GET | 商品列表 |
| `/api/products/{id}` | GET | 商品详情 |
| `/api/orders` | GET/POST | 订单管理 |
| `/api/orders/{id}` | GET | 订单详情 |
| `/api/pay/create` | POST | 创建支付 |
| `/api/pay/callback` | POST | 支付回调 |

## 开发指南

### 代码规范

使用 Maven Checkstyle 插件进行代码风格检查：

```bash
./mvnw checkstyle:check
```

### 运行测试

```bash
./mvnw test
```

### 构建打包

```bash
./mvnw package -DskipTests
```

## 相关文档

- [微信支付配置指南](WECHAT_PAY_SETUP.md)
- [Docker 部署指南](docs/deployment/docker.md)
- [生产环境部署指南](PRODUCTION_DEPLOY.md)
- [API 文档](docs/api/README.md)
- [更新日志](CHANGELOG.md)

## 许可证

本项目仅供个人学习使用。

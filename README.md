# MiniMall 微信小程序商城

基于 Spring Boot + PostgreSQL + Docker 的微信小程序电商平台。

## 项目架构

```
minimall/
├── src/                      # 后端源代码
│   └── main/
│       ├── java/             # Java 源代码
│       └── resources/        # 配置文件
├── backend/                  # 后端模块（待整合）
├── miniprogram/              # 微信小程序前端
├── docs/                     # 文档目录
│   ├── api/                  # API 文档
│   ├── user-guide/           # 用户指南
│   ├── deployment/           # 部署文档
│   └── monitoring/           # 监控配置
├── checkstyle.xml            # 代码风格检查配置
├── pom.xml                   # Maven 项目配置
└── docker-compose.yml        # Docker 编排配置
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.x |
| 数据库 | PostgreSQL |
| 构建工具 | Maven |
| 容器化 | Docker + Docker Compose |
| 支付集成 | 微信支付 API v3 |
| 代码检查 | Checkstyle |
| API 文档 | OpenAPI/Swagger |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- PostgreSQL 14+
- Docker & Docker Compose

### 本地开发

```bash
# 1. 克隆项目
git clone https://github.com/iivum/minimall.git
cd minimall

# 2. 配置数据库
# 创建 PostgreSQL 数据库
createdb minimall

# 3. 配置环境变量
export POSTGRES_PASSWORD=your_password
export WECHATPAY_MCHID=your_mchid
export WECHATPAY_SERIAL_NO=your_serial
export WECHATPAY_API_V3_KEY=your_key

# 4. 启动依赖服务
docker-compose up -d postgres

# 5. 运行应用
mvn spring-boot:run

# 6. 运行检查
mvn checkstyle:check
mvn test
```

### Docker 部署

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
```

## API 文档

部署后访问: `http://localhost:8080/swagger-ui.html`

完整文档见 [docs/api/README.md](docs/api/README.md)

## 代码规范

- 使用 Checkstyle 进行代码风格检查
- 遵循 [code-merge-checklist.md](code-merge-checklist.md) 进行代码合并验证
- 日志级别规范：见 checkstyle.xml 注释

## 部署指南

详见 [PRODUCTION_DEPLOY.md](PRODUCTION_DEPLOY.md)

## 微信支付配置

详见 [WECHAT_PAY_SETUP.md](WECHAT_PAY_SETUP.md)

## 开发指南

### 分支规范

- `main` - 主分支，稳定版本
- `agent/{name}/{task}` - Agent 工作分支
- `feature/*` - 功能分支

### 提交规范

```
<type>: <description>

Types: feat, fix, refactor, docs, test, chore, perf, ci
```

### 代码合并检查

所有 PR 必须执行 [code-merge-checklist.md](code-merge-checklist.md) 中的检查项。

## 许可证

MIT License - 详见 [LICENSE](LICENSE)

## 版本历史

详见 [CHANGELOG.md](CHANGELOG.md)
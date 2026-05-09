# MiniMall 微信小程序商城

MiniMall 是一款基于 Spring Boot + 微信小程序的轻量级电商平台，支持商品管理、订单处理、微信支付和会员积分体系。

## 项目架构

```
minimall/
├── backend/              # Spring Boot 后端服务
├── miniprogram/          # 微信小程序前端
├── docs/                 # 技术文档
│   ├── api/              # API 接口文档
│   ├── deployment/        # 部署文档
│   ├── monitoring/       # 监控配置 (Prometheus/Grafana)
│   └── meetings/          # 会议纪要
├── src/                  # Java 源代码
└── docker-compose.yml    # Docker 部署配置
```

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.2.5 | Web 框架 |
| Spring Security | 6.x | 安全认证 |
| Spring Data JPA | 3.x | ORM |
| PostgreSQL | 15 | 主数据库 |
| JWT | 0.12.5 | Token 认证 |
| SpringDoc OpenAPI | 2.5.0 | API 文档 |
| WeChat Pay SDK | 0.2.17 | 微信支付 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| 微信小程序 | - | 跨平台应用 |
| JavaScript | ES6+ | 编程语言 |

### 运维

| 技术 | 版本 | 用途 |
|------|------|------|
| Docker | 20.10+ | 容器化 |
| Docker Compose | 2.0+ | 多容器编排 |
| Prometheus | v2.50.0 | 指标采集 |
| Grafana | 10.4.0 | 可视化监控 |
| Alertmanager | v0.27.0 | 告警管理 |

## 快速开始

### 环境要求

- JDK 17+
- Docker 20.10+
- Docker Compose 2.0+
- 微信小程序开发者工具

### 1. 克隆项目

```bash
git clone https://github.com/iivum/minimall.git
cd minimall
```

### 2. 配置环境变量

```bash
# 创建 .env 文件
cat > .env << EOF
# Database
POSTGRES_PASSWORD=your_secure_password

# WeChat Pay
WECHATPAY_MCHID=your_merchant_id
WECHATPAY_SERIAL_NO=your_serial_number
WECHATPAY_API_V3_KEY=your_api_v3_key
WECHATPAY_CALLBACK_URL=https://your-domain.com/api/pay/callback
WECHATPAY_SANDBOX=false

# Ports
POSTGRES_PORT=5432
BACKEND_PORT=8080
EOF
```

### 3. 启动服务

```bash
# 启动所有服务 (后端 + 数据库 + 监控)
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看后端日志
docker-compose logs -f backend
```

### 4. 验证部署

```bash
# 健康检查
curl http://localhost:8080/health

# Swagger API 文档
curl http://localhost:8080/swagger-ui.html
```

## 功能模块

### 核心功能

- **商品管理**: 商品浏览、搜索、详情展示
- **购物车**: 加购、数量修改、结算
- **订单处理**: 创建订单、支付、取消、状态跟踪
- **微信支付**: JSAPI 支付、支付回调
- **会员体系**: 会员等级、积分累积与兑换
- **优惠券**: 优惠券领取与使用
- **直播**: 微信小程序直播功能
- **分享**: 商品分享

### API 端点概览

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `/api/auth/*` | 登录、Token 刷新 |
| 用户 | `/api/user/*` | 用户信息管理 |
| 商品 | `/api/products/**` | 商品查询、详情 |
| 购物车 | `/api/cart/**` | 购物车管理 |
| 订单 | `/api/orders/**` | 订单创建、查询、取消 |
| 支付 | `/api/pay/**` | 微信支付相关 |
| 会员 | `/api/membership/**` | 会员等级、积分 |
| 优惠券 | `/api/coupons/**` | 优惠券管理 |
| 管理后台 | `/api/admin/**` | 后台管理 (管理员) |
| 统计 | `/api/stats/**` | 数据统计 |

完整 API 文档请参考 [docs/api/README.md](docs/api/README.md)。

## 微信支付配置

1. 登录 [微信商户平台](https://pay.weixin.qq.com)
2. 获取商户号 (MCHID) 和序列号
3. 在「API 安全」中设置 APIv3 密钥
4. 将证书文件放入 `src/main/resources/cert/`:
   - `apiclient_key.pem` - 商户私钥
   - `apiclient_cert.pem` - 商户证书
5. 配置回调地址 (公网 HTTPS)

详细配置说明请参考 [WECHAT_PAY_SETUP.md](WECHAT_PAY_SETUP.md)。

## 项目结构

### 后端包结构

```
com.minimall
├── controller/      # REST API 控制器
├── service/         # 业务逻辑
├── repository/      # 数据访问层
├── model/           # JPA 实体
├── dto/             # 数据传输对象
├── config/          # 配置类
├── exception/       # 异常处理
└── util/            # 工具类
```

### 前端页面结构

```
miniprogram/pages/
├── index/           # 首页 - 商品列表
├── product/         # 商品详情
├── cart/            # 购物车
├── order/           # 订单
├── member/          # 会员中心
└── pay/             # 支付页面
```

## 部署文档

- [Docker 部署指南](docs/deployment/docker.md)
- [生产环境部署](PRODUCTION_DEPLOY.md)
- [监控告警配置](docs/monitoring/ALERTING.md)

## API 文档

- [Swagger UI](http://localhost:8080/swagger-ui.html) - 运行时访问
- [OpenAPI JSON](http://localhost:8080/v3/api-docs) - OpenAPI 规范

## 监控指标

部署后可通过以下地址访问监控面板:

| 服务 | 地址 | 默认凭证 |
|------|------|----------|
| Grafana | http://localhost:3000 | admin/admin123 |
| Prometheus | http://localhost:9090 | - |
| Alertmanager | http://localhost:9093 | - |

## 开发

### 本地开发

```bash
# 编译项目
./mvnw clean package

# 运行测试
./mvnw test

# 运行后端 (dev profile)
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

### 微信小程序开发

1. 使用微信开发者工具打开 `miniprogram/` 目录
2. 配置 AppID
3. 导入项目
4. 修改 `app.js` 中的 API 地址为本地后端地址

## 相关文档

- [更新日志](CHANGELOG.md)
- [微信支付配置](WECHAT_PAY_SETUP.md)
- [监控告警](docs/monitoring/ALERTING.md)

## License

See [LICENSE](LICENSE) for details.

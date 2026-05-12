# MiniMall 常见问题 (FAQ)

## 目录
- [开发环境](#开发环境)
- [微信支付](#微信支付)
- [API 问题](#api-问题)
- [部署相关](#部署相关)

---

## 开发环境

### Q: 本地开发需要哪些依赖？
**A:** 需要以下环境：
- Java 17+
- Maven 3.8+
- Docker & Docker Compose（用于数据库）
- 微信开发者工具（小程序开发）

详见 [README.md](../README.md#快速开始)

### Q: H2 数据库适合生产环境吗？
**A:** 不适合。H2 是内存数据库，仅用于开发环境。生产环境请使用 PostgreSQL，配置方法见 [README.md](../README.md#生产模式-postgresql)。

### Q: 如何解决 Maven 依赖下载慢的问题？
**A:** 可以使用 Maven 镜像加速。在 `~/.m2/settings.xml` 中配置阿里云镜像：
```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

---

## 微信支付

### Q: 微信支付回调需要公网访问吗？
**A:** 是的，微信支付回调需要 HTTPS 公网可访问的地址。开发阶段可以使用 ngrok 或内网穿透工具进行测试。

### Q: 支付回调地址如何配置？
**A:** 在 `application.yml` 中配置：
```yaml
wechat:
  pay:
    callback-url: https://your-domain.com/api/pay/callback
```

### Q: 沙箱环境如何使用？
**A:** 微信支付沙箱环境需要单独申请，配置不同的商户号和密钥。开发阶段建议使用真实的测试商户号。

---

## API 问题

### Q: 请求返回 401 未授权？
**A:** 请检查：
1. 请求 header 中是否包含 `Authorization: Bearer <token>`
2. token 是否在有效期内
3. token 格式是否正确

### Q: 如何获取 JWT token？
**A:** 调用登录接口 `POST /api/auth/login`，传入微信 openid，返回 token。

### Q: 分页参数如何使用？
**A:** 商品列表接口示例：
```
GET /api/products?page=0&size=10&sort=price-asc
```
- `page`: 页码，从 0 开始
- `size`: 每页数量，默认 10
- `sort`: 排序字段，可选 `price-asc`, `price-desc`

---

## 部署相关

### Q: Docker 容器无法启动？
**A:** 排查步骤：
1. 检查端口占用：`docker-compose ps`
2. 查看日志：`docker-compose logs backend`
3. 验证环境变量：确保 `.env` 文件存在且配置正确

### Q: 如何查看应用健康状态？
**A:** 访问健康检查端点：
```
GET /health
```
返回应用状态和数据库连接状态。

### Q: 数据库迁移如何执行？
**A:** JPA 会自动创建表结构（配置 `spring.jpa.hibernate.ddl-auto=update`）。如需手动迁移，使用 Flyway 或 Liquibase。

---

## 其他问题

### Q: 如何联系项目维护者？
**A:** 通过 GitHub Issues 提出问题，标签选择 `question`。

### Q: 代码合并前需要检查什么？
**A:** 请参考 [code-merge-checklist.md](../code-merge-checklist.md) 进行检查。

---

## 相关文档
- [README.md](../README.md)
- [PRODUCTION_DEPLOY.md](../PRODUCTION_DEPLOY.md)
- [API 文档](api/README.md)
- [部署指南](deployment/docker.md)
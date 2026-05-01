# Minimall v1.0 生产环境部署指南

## Phase 10: 生产环境部署与正式上线

### 一、生产环境配置清单

#### 1.1 环境变量配置

在生产服务器上创建 `.env` 文件：

```bash
# Database
POSTGRES_PASSWORD=your_secure_postgres_password

# WeChat Pay (必填)
WECHATPAY_MCHID=your_merchant_id
WECHATPAY_SERIAL_NO=your_serial_number
WECHATPAY_API_V3_KEY=your_api_v3_key
WECHATPAY_CALLBACK_URL=https://your-domain.com/api/pay/callback
WECHATPAY_SANDBOX=false

# Ports
POSTGRES_PORT=5432
BACKEND_PORT=8080
```

#### 1.2 微信支付配置

1. **商户号配置**: 登录微信商户平台获取
2. **APIv3密钥**: 在商户平台 → API安全 设置
3. **证书配置**: 将证书文件放入 `src/main/resources/cert/` 目录
   - `apiclient_key.pem` - 商户私钥
   - `apiclient_cert.pem` - 商户证书
4. **回调地址**: 配置公网可访问的HTTPS地址

### 二、Docker Compose 部署

#### 2.1 启动服务

```bash
# 进入项目目录
cd minimall

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
```

#### 2.2 健康检查

```bash
# 检查后端健康状态
curl http://localhost:8080/health

# 检查Swagger UI
curl http://localhost:8080/swagger-ui.html
```

#### 2.3 停止服务

```bash
docker-compose down
```

### 三、域名和HTTPS配置

#### 3.1 Nginx 反向代理配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 强制HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    server_name your-domain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    location / {
        proxy_pass http://localhost:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### 3.2 证书申请 (Let's Encrypt)

```bash
# 安装certbot
brew install certbot  # macOS
# apt install certbot  # Ubuntu/Debian

# 申请证书
certbot certonly --standalone -d your-domain.com

# 自动续期配置
certbot renew --dry-run
```

### 四、微信小程序发布

#### 4.1 提交流程

1. **登录微信公众平台**: https://mp.weixin.qq.com
2. **版本管理**: 进入「版本管理」
3. **提交审核**: 点击「提交审核」
4. **填写信息**:
   - 版本说明
   - 类目选择
   - 功能页面
5. **等待审核**: 预计1-7个工作日

#### 4.2 审核跟进

- 登录微信公众平台查看审核进度
- 如有驳回，根据反馈修改后重新提交
- 审核通过后即可发布上线

### 五、监控与告警配置

#### 5.1 健康检查端点

后端提供以下健康检查端点：
- `/health` - 服务健康状态
- `/api-docs` - OpenAPI文档

#### 5.2 日志监控

```bash
# 查看后端日志
docker-compose logs -f backend

# 查看错误日志
docker-compose logs -f backend | grep ERROR
```

#### 5.3 建议的监控指标

- CPU/内存使用率
- 数据库连接数
- API响应时间
- 支付成功率
- 订单处理量

### 六、故障排查

#### 6.1 常见问题

| 问题 | 解决方案 |
|------|----------|
| 数据库连接失败 | 检查POSTGRES_PASSWORD和网络连接 |
| 支付回调失败 | 确认回调URL公网可访问且为HTTPS |
| 服务启动失败 | 检查日志 `docker-compose logs backend` |
| 前端无法访问 | 检查nginx配置和端口映射 |

#### 6.2 日志查看

```bash
# 所有服务日志
docker-compose logs

# 指定服务日志
docker-compose logs postgres
docker-compose logs backend

# 实时跟踪
docker-compose logs -f
```

### 七、数据备份

#### 7.1 数据库备份

```bash
# 备份数据库
docker exec minimall-postgres pg_dump -U minimall minimall > backup.sql

# 恢复数据库
docker exec -i minimall-postgres psql -U minimall minimall < backup.sql
```

#### 7.2 定时备份 (crontab)

```bash
# 每天凌晨2点备份
0 2 * * * docker exec minimall-postgres pg_dump -U minimall minimall > /backup/minimall_$(date +\%Y\%m\%d).sql
```

### 八、安全检查清单

- [ ] 微信支付商户号和API密钥已配置
- [ ] 数据库密码已更改默认密码
- [ ] HTTPS证书已配置
- [ ] 回调URL为HTTPS公网地址
- [ ] 防火墙仅开放必要端口
- [ ] 定期查看安全日志

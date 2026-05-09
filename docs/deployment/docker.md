# Docker 部署指南

## 环境要求

- Docker 20.10+
- Docker Compose 2.0+
- 2GB RAM minimum
- 20GB disk space

## 快速开始

```bash
# 克隆项目
git clone https://github.com/iivum/minimall.git
cd minimall

# 配置环境变量
cp .env.example .env
# 编辑 .env 文件，填写必要的配置

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看后端日志
docker-compose logs -f backend

# 健康检查
curl http://localhost:8080/health
```

## 环境变量

| 变量 | 说明 | 必填 |
|------|------|------|
| `POSTGRES_PASSWORD` | PostgreSQL 密码 | 是 |
| `POSTGRES_PORT` | PostgreSQL 端口 | 否，默认 5432 |
| `BACKEND_PORT` | 后端服务端口 | 否，默认 8080 |
| `WECHATPAY_MCHID` | 微信商户号 | 是 |
| `WECHATPAY_SERIAL_NO` | 微信支付序列号 | 是 |
| `WECHATPAY_API_V3_KEY` | 微信 APIv3 密钥 | 是 |
| `WECHATPAY_CALLBACK_URL` | 微信支付回调地址 | 是 |
| `WECHATPAY_SANDBOX` | 启用沙箱环境 | 否，默认 false |
| `GRAFANA_PASSWORD` | Grafana 管理密码 | 否，默认 admin123 |

## 服务说明

docker-compose 包含以下服务：

| 服务 | 端口 | 说明 |
|------|------|------|
| `postgres` | 5432 | PostgreSQL 15 数据库 |
| `backend` | 8080 | Spring Boot 后端服务 |
| `prometheus` | 9090 | Prometheus 监控 |
| `grafana` | 3000 | Grafana 可视化面板 |
| `alertmanager` | 9093 | Alertmanager 告警 |

## 数据库初始化

首次启动时，数据库会自动创建。无需手动执行迁移。

## 故障排查

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看后端日志
docker-compose logs -f backend

# 重启后端服务
docker-compose restart backend

# 重建服务（清除数据）
docker-compose down -v && docker-compose up -d --build
```

## 证书配置

微信支付需要的证书文件：

1. 将证书文件放入 `src/main/resources/cert/` 目录
2. 文件名必须为：
   - `apiclient_key.pem` - 商户私钥
   - `apiclient_cert.pem` - 商户证书

## 监控配置

Prometheus 和 Grafana 已经配置好，可直接访问：

- Grafana: http://localhost:3000 (admin/admin123)
- Prometheus: http://localhost:9090
# Docker Deployment Guide

## Prerequisites

- Docker 20.10+
- Docker Compose 2.0+
- 2GB RAM minimum
- 20GB disk space

## Quick Start

```bash
git clone git@github.com:iivum/minimall.git
cd minimall
cp .env.example .env
docker-compose up -d
docker-compose exec app npm run db:migrate
curl http://localhost:3000/health
```

## Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `NODE_ENV` | Environment mode | Yes |
| `PORT` | Application port | Yes |
| `DB_HOST` | PostgreSQL host | Yes |
| `DB_PORT` | PostgreSQL port | Yes |
| `DB_NAME` | Database name | Yes |
| `DB_USER` | Database user | Yes |
| `DB_PASSWORD` | Database password | Yes |
| `REDIS_HOST` | Redis host | Yes |
| `REDIS_PORT` | Redis port | Yes |
| `WECHAT_APP_ID` | WeChat App ID | Yes |
| `WECHAT_MCH_ID` | WeChat Merchant ID | Yes |
| `WECHAT_API_KEY` | WeChat API Key | Yes |
| `JWT_SECRET` | JWT Secret (32+ chars) | Yes |

## Database Migration

```bash
docker-compose exec app npm run db:migrate
docker-compose exec app npm run db:migrate:rollback
```

## Troubleshooting

```bash
docker-compose logs -f app
docker-compose restart app
docker-compose down -v && docker-compose up -d --build
```
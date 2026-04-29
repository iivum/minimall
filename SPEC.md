# Minimall 规格说明书

## 1. 项目概述

**项目名称：** Minimall
**项目类型：** 微信小程序电商系统
**核心功能：** 面向小程序的商品浏览、购物车、订单管理功能
**目标用户：** 微信小程序用户

## 2. 技术栈

### 后端
- Spring Boot 3.2.0
- Java 17
- PostgreSQL 15
- MyBatis-Plus 3.5.5

### 前端
- 微信小程序
- 管理后台: React + Ant Design

## 3. 功能模块

### 3.1 用户模块
- 微信授权登录
- 用户信息管理

### 3.2 商品模块
- 商品列表（分类筛选）
- 商品详情
- 库存管理

### 3.3 分类模块
- 分类列表
- 分类管理

### 3.4 订单模块
- 创建订单
- 订单列表
- 订单状态管理

## 4. 数据库设计

### 表结构

**users** - 用户表
- id (BIGSERIAL PRIMARY KEY)
- openid (VARCHAR, UNIQUE)
- nickname, avatar_url, phone
- created_at, updated_at

**categories** - 分类表
- id (BIGSERIAL PRIMARY KEY)
- name, icon, sort_order, is_active
- created_at, updated_at

**products** - 商品表
- id (BIGSERIAL PRIMARY KEY)
- name, description, price, original_price
- category_id (FK), image_url, images
- inventory_count, is_active
- created_at, updated_at

**orders** - 订单表
- id (BIGSERIAL PRIMARY KEY)
- order_no (UNIQUE)
- user_id (FK), total_amount
- status, receiver_name, receiver_phone, receiver_address, remark
- created_at, updated_at

**order_items** - 订单项表
- id (BIGSERIAL PRIMARY KEY)
- order_id (FK), product_id (FK)
- product_name, price, quantity
- created_at

## 5. API 设计

### 用户 API
- `POST /api/users/login` - 微信登录

### 商品 API
- `GET /api/products` - 商品列表
- `GET /api/products/{id}` - 商品详情

### 分类 API
- `GET /api/categories` - 分类列表

### 订单 API
- `POST /api/orders` - 创建订单
- `GET /api/orders` - 订单列表
- `GET /api/orders/{id}` - 订单详情
- `PUT /api/orders/{id}/status` - 更新订单状态

## 6. 开发进度

### Phase 1 (当前)
- [x] 后端架构搭建 (MIN-13)
- [ ] 管理后台前端搭建 (MIN-14)
- [ ] 微信小程序搭建 (MIN-15)

## 7. 验收标准

### 后端验收
- 服务能通过 `./mvnw spring-boot:run` 启动
- PostgreSQL 连接成功
- 访问 localhost:8080 能看到 Spring Boot 启动日志

# MiniMall API Documentation

## Overview

MiniMall 微信小程序商城后端 API，基于 Spring Boot 3.2.5，提供商品管理、订单处理、微信支付和会员积分等功能的 RESTful 接口。

**Base URL:** `http://localhost:8080` (本地开发) / `https://api.minimall.com` (生产)

**API 文档:** Swagger UI 位于 `/swagger-ui.html`

**认证方式:** Bearer Token (JWT) 放在 Authorization Header 中

```
Authorization: Bearer <access_token>
```

---

## 认证 Authentication

### POST /api/auth/login

微信登录，获取访问令牌。

**Request:**
```json
{
  "code": "string"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "access_token": "string",
    "expires_in": 7200,
    "user": {
      "id": "string",
      "openid": "string",
      "role": "customer|admin"
    }
  }
}
```

### POST /api/auth/refresh

刷新访问令牌。

**Request:**
```json
{
  "refresh_token": "string"
}
```

---

## 用户 User

### GET /api/user/{id}

获取用户信息。

### PUT /api/user/{id}

更新用户信息。

---

## 商品 Product

### GET /api/products

获取商品列表（分页）。

**Query Parameters:**
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | int | 0 | 页码 |
| size | int | 10 | 每页数量 |
| search | string | - | 搜索商品名称 |
| sort | string | - | 排序: price-asc, price-desc, stock-asc, stock-desc |

### GET /api/products/all

获取所有商品（不分页）。

### GET /api/products/{id}

获取商品详情。

### POST /api/products

创建商品（管理员）。

### PUT /api/products/{id}

更新商品（管理员）。

### DELETE /api/products/{id}

删除商品（管理员）。

---

## 购物车 Cart

### GET /api/cart/{userId}

获取用户购物车。

### POST /api/cart/{userId}/items

添加商品到购物车。

**Request:**
```json
{
  "productId": "string",
  "quantity": 1
}
```

### PUT /api/cart/{userId}/items/{itemId}

更新购物车商品数量。

### DELETE /api/cart/{userId}/items/{itemId}

从购物车移除商品。

---

## 订单 Order

### GET /api/orders/user/{userId}

获取用户的订单列表。

### GET /api/orders/{id}

获取订单详情。

### GET /api/orders/no/{orderNo}

通过订单号获取订单。

### POST /api/orders

创建新订单。

**Request:**
```json
{
  "userId": "string",
  "items": [
    {
      "productId": "string",
      "quantity": 1
    }
  ],
  "couponId": "string"
}
```

### PATCH /api/orders/{id}/status

更新订单状态 (PENDING_PAYMENT, PAID, SHIPPED, COMPLETED, CANCELLED)。

### PATCH /api/orders/{id}/pay

标记订单已支付。

---

## 支付 Payment

### POST /api/pay/create/{orderId}

创建微信支付统单订单，获取预支付 id。

### POST /api/pay/callback

微信支付回调通知。

---

## 会员 Membership

### GET /api/membership/{userId}

获取会员信息。

### POST /api/membership/{userId}/points/add

增加用户积分。

---

## 优惠券 Coupon

### GET /api/coupons

获取可用优惠券列表。

### POST /api/coupons/{userId}/claim/{couponId}

用户领取优惠券。

### POST /api/coupons/use

使用优惠券。

---

## 统计 Stats

### GET /api/stats/overview

获取数据概览。

### GET /api/stats/sales

获取销售统计。

---

## 管理后台 Admin

### GET /api/admin/products

后台商品管理列表。

### GET /api/admin/orders

后台订单列表。

### GET /api/admin/users

用户列表。

---

## 错误码 Error Codes

| 错误码 | HTTP 状态 | 说明 |
|--------|-----------|------|
| `INVALID_PARAMETER` | 400 | 请求参数无效 |
| `UNAUTHORIZED` | 401 | 需要认证 |
| `FORBIDDEN` | 403 | 权限不足 |
| `NOT_FOUND` | 404 | 资源不存在 |
| `OUT_OF_STOCK` | 400 | 商品库存不足 |
| `ORDER_INVALID` | 400 | 订单无法处理 |
| `PAYMENT_FAILED` | 400 | 支付失败 |
| `RATE_LIMITED` | 429 | 请求过于频繁 |
| `INTERNAL_ERROR` | 500 | 服务器内部错误 |

---

## 健康检查 Health Check

### GET /health

服务健康状态检查。

**Response:**
```json
{
  "status": "UP"
}
```
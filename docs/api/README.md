# MiniMall API Documentation

微信小程序商城 RESTful API 文档，基于 SpringDoc OpenAPI 3。

**Base URL:** `http://localhost:8080/api`

**认证:** Bearer Token (JWT) 在 Authorization header 中传递

**Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## 认证 Authentication

### POST /api/auth/login
登录获取 JWT token。

**Request:**
```json
{
  "openid": "微信用户openid"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "userId": "user_123"
}
```

---

### POST /api/auth/register
注册新用户。

**Request:**
```json
{
  "openid": "微信用户openid",
  "nickname": "用户名",
  "phone": "13800138000",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "userId": "user_123"
}
```

---

## 商品 Products

### GET /api/products
获取商品列表（分页）。

**Query Parameters:**
| 参数 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `page` | int | 0 | 页码 |
| `size` | int | 10 | 每页数量 |
| `search` | string | - | 按名称搜索 |
| `sort` | string | - | 排序: `price-asc`, `price-desc`, `stock-asc`, `stock-desc` |

**Response:**
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0
}
```

---

### GET /api/products/all
获取所有商品（非分页）。

---

### GET /api/products/{id}
获取商品详情。

---

### GET /api/products/search?name=xxx
搜索商品（按名称，非分页）。

---

### POST /api/products
创建商品（需管理员权限）。

**Request:**
```json
{
  "name": "商品名称",
  "description": "商品描述",
  "price": 99.00,
  "stock": 100
}
```

---

### PUT /api/products/{id}
更新商品。

---

### DELETE /api/products/{id}
删除（停用）商品。

---

## 订单 Orders

### POST /api/orders
创建新订单。

**Request:**
```json
{
  "userId": "user_123",
  "items": [
    {"productId": "prod_001", "quantity": 2}
  ]
}
```

**Response:**
```json
{
  "id": "order_123",
  "orderNo": "MM20240101123456",
  "status": "PENDING_PAYMENT",
  "totalAmount": 198.00,
  ...
}
```

---

### GET /api/orders/user/{userId}
获取用户的订单列表。

---

### GET /api/orders/{id}
获取订单详情。

---

### GET /api/orders/no/{orderNo}
通过订单号获取订单。

---

### PATCH /api/orders/{id}/status
更新订单状态。

**Query Parameters:**
- `status`: `PENDING_PAYMENT`, `PAID`, `SHIPPED`, `COMPLETED`, `CANCELLED`

---

### PATCH /api/orders/{id}/pay
标记订单已支付。

**Query Parameters:**
- `tradeNo`: 微信交易号

---

## 支付 Payment

### POST /api/pay/create/{orderId}
创建微信支付统一下单，返回调起支付的签名参数。

**Query Parameters:**
- `openid`: 微信用户 openid

**Response:**
```json
{
  "timeStamp": "1704067200",
  "nonceStr": "uuid随机字符串",
  "package": "prepay_id=wx...",
  "signType": "RSA",
  "paySign": "调起签名"
}
```

---

### POST /api/pay/callback
微信支付回调通知（需公网 HTTPS 访问）。

---

## 会员 Membership

### GET /api/membership/benefits
获取当前用户会员权益。

---

### POST /api/membership/redeem
兑换会员权益。

**Request:**
```json
{
  "benefitType": "DISCOUNT",
  "amount": 10.00
}
```

---

## 积分 Points

### GET /api/points/account
获取当前用户积分账户。

**Response:**
```json
{
  "userId": "user_123",
  "balance": 500,
  "grade": "GOLD"
}
```

---

### GET /api/points/account/{userId}
获取指定用户积分账户。

---

### GET /api/points/history
获取当前用户积分变动历史。

---

### GET /api/points/history/{userId}
获取指定用户积分变动历史。

---

### POST /api/points/sign-in
签到获取积分。

---

### POST /api/points/earn/share/{shareId}
分享获取积分奖励。

---

### POST /api/points/deduct
扣除积分。

**Request:**
```json
{
  "points": 100,
  "orderNo": "ORDER123",
  "description": "积分抵扣"
}
```

---

### POST /api/points/redeem/coupon
积分兑换优惠券。

**Request:**
```json
{
  "points": 200,
  "orderNo": "ORDER123"
}
```

---

## 优惠券 Coupons

### POST /api/coupons
创建优惠券（管理员）。

**Request:**
```json
{
  "name": "新人专享券",
  "type": "NEW_USER",
  "discount": 10.00,
  "minAmount": 50.00,
  "validDays": 30
}
```

---

### GET /api/coupons
获取所有可用优惠券。

---

### GET /api/coupons/new-user
获取新人专属优惠券。

---

### POST /api/coupons/{couponId}/claim
领取优惠券。

**Headers:**
- `X-User-Id`: 用户ID

---

### GET /api/coupons/my
获取用户已领取的优惠券。

**Headers:**
- `X-User-Id`: 用户ID

---

## 客服 Customer Service

### POST /api/customer-service/receive
接收用户客服消息。

**Query Parameters:**
- `openid`: 微信用户 openid
- `content`: 消息内容
- `type`: 消息类型 (`TEXT`, `IMAGE`, `VOICE`)

---

### GET /api/customer-service/messages/{openid}
获取用户的客服消息历史。

---

### GET /api/customer-service/pending
获取所有待处理消息（客服工作台）。

---

### POST /api/customer-service/{messageId}/read
标记消息为已读。

---

### POST /api/customer-service/{messageId}/complete
标记消息为已完成。

---

### POST /api/customer-service/{messageId}/transfer?handlerId=xxx
转人工客服。

---

### GET /api/customer-service/stats/pending-count
获取待处理消息数量。

---

## 图片上传 Upload

### POST /api/upload/image
上传图片文件。

**Form Data:**
- `file`: 图片文件（最大 2MB）

**Response:**
```json
{
  "url": "/uploads/uuid-filename.jpg"
}
```

---

### POST /api/upload/image/base64
上传 Base64 编码的图片。

**Request:**
```json
{
  "image": "data:image/png;base64,iVBORw0KGgo..."
}
```

---

## 分类 Category (Admin)

### GET /api/admin/categories
获取所有启用的分类。

---

### GET /api/admin/categories/{id}
获取分类详情。

---

### POST /api/admin/categories
创建分类。

**Request:**
```json
{
  "name": "服装",
  "sortOrder": 1,
  "parentId": null
}
```

---

### PUT /api/admin/categories/{id}
更新分类。

---

### DELETE /api/admin/categories/{id}
删除（停用）分类。

---

## 统计 Stats (Admin)

### GET /api/admin/stats/dashboard
获取仪表盘统计数据。

**Response:**
```json
{
  "todayOrders": 25,
  "todayRevenue": 12500.00,
  "totalProducts": 150,
  "pendingOrders": 8
}
```

---

### GET /api/admin/stats/orders-trend?days=30
获取最近 N 天的订单趋势。

---

## 错误码 Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
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

## 开发

API 通过 SpringDoc OpenAPI 自动生成，访问 Swagger UI 查看完整交互式文档。
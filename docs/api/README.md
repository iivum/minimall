# MiniMall API Documentation

## Overview

MiniMall is a WeChat mini-program e-commerce platform providing RESTful APIs for product management, order processing, and payment integration.

**Base URL:** `https://api.minimall.com/v1`

**Authentication:** Bearer token (JWT) in Authorization header

---

## Authentication

### POST /api/auth/login

Login with WeChat openid to get access token.

**Request:**
```json
{
  "openid": "string"
}
```

**Response:**
```json
{
  "token": "string",
  "userId": "string"
}
```

### POST /api/auth/register

Register a new user.

**Request:**
```json
{
  "openid": "string",
  "nickname": "string",
  "phone": "string",
  "avatarUrl": "string"
}
```

**Response:**
```json
{
  "token": "string",
  "userId": "string"
}
```

---

## Products

### GET /api/products

Get all active products (paginated).

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | Page number |
| size | int | 10 | Page size |
| search | string | - | Search by product name |
| sort | string | - | Sort: price-asc, price-desc, stock-asc, stock-desc |

**Response:**
```json
{
  "content": [
    {
      "id": "string",
      "name": "string",
      "description": "string",
      "price": 0.00,
      "stock": 0,
      "imageUrl": "string",
      "category": "string",
      "active": true,
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0
}
```

### GET /api/products/all

Get all active products (non-paginated).

**Response:**
```json
[
  {
    "id": "string",
    "name": "string",
    "price": 0.00,
    "stock": 0
  }
]
```

### GET /api/products/{id}

Get product by ID.

### GET /api/products/search

Search products by name (non-paginated).

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| name | string | Product name to search |

### POST /api/products

Create a new product (admin).

**Request:**
```json
{
  "name": "string",
  "description": "string",
  "price": 0.00,
  "stock": 0,
  "imageUrl": "string",
  "category": "string"
}
```

### PUT /api/products/{id}

Update a product.

### DELETE /api/products/{id}

Delete (deactivate) a product.

---

## Orders

### GET /api/orders/user/{userId}

Get orders by user ID.

### GET /api/orders/{id}

Get order by ID.

### GET /api/orders/no/{orderNo}

Get order by order number.

### POST /api/orders

Create a new order.

**Request:**
```json
{
  "userId": "string",
  "items": [
    {
      "productId": "string",
      "quantity": 1,
      "price": 0.00
    }
  ]
}
```

**Response:**
```json
{
  "id": "string",
  "orderNo": "string",
  "userId": "string",
  "totalAmount": 0.00,
  "status": "PENDING|PAID|SHIPPED|COMPLETED|CANCELLED",
  "items": [],
  "createdAt": "2024-01-01T00:00:00Z"
}
```

### PATCH /api/orders/{id}/status

Update order status.

### PATCH /api/orders/{id}/pay

Mark order as paid.

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| tradeNo | string | Payment transaction number |

---

## Payment

### POST /api/pay/create/{orderId}

Create unified WeChat payment order and get prepay_id.

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| openid | string | User's WeChat openid |

**Response:**
```json
{
  "timeStamp": 1234567890,
  "nonceStr": "string",
  "package": "prepay_id=string",
  "signType": "RSA",
  "paySign": "string"
}
```

### POST /api/pay/callback

WeChat Pay callback notification.

**Headers:**
| Header | Description |
|--------|-------------|
| Wechatpay-Signature | Callback signature |
| Wechatpay-Serial | Certificate serial number |

---

## Coupons

### GET /api/coupons

Get all available coupons.

**Response:**
```json
[
  {
    "id": "string",
    "name": "string",
    "type": "DISCOUNT|FIXED",
    "value": 0,
    "minAmount": 0.00,
    "startDate": "2024-01-01",
    "endDate": "2024-12-31"
  }
]
```

### GET /api/coupons/new-user

Get new user exclusive coupons.

### POST /api/coupons

Create a new coupon (admin).

**Request:**
```json
{
  "name": "string",
  "type": "DISCOUNT|FIXED",
  "value": 0,
  "minAmount": 0.00,
  "totalCount": 100,
  "startDate": "2024-01-01",
  "endDate": "2024-12-31"
}
```

### POST /api/coupons/{couponId}/claim

Claim a coupon.

**Headers:**
| Header | Description |
|--------|-------------|
| X-User-Id | User ID |

### GET /api/coupons/my

Get user's claimed coupons.

**Headers:**
| Header | Description |
|--------|-------------|
| X-User-Id | User ID |

---

## Categories (Admin)

### GET /api/admin/categories

List all active categories.

### GET /api/admin/categories/{id}

Get category by ID.

### POST /api/admin/categories

Create new category.

**Request:**
```json
{
  "name": "string",
  "sortOrder": 0,
  "active": true,
  "parentId": "string"
}
```

### PUT /api/admin/categories/{id}

Update category.

### DELETE /api/admin/categories/{id}

Soft delete category.

---

## Admin Orders

### GET /api/admin/orders

Get all orders (admin).

### GET /api/admin/orders/{id}

Get order by ID (admin).

### GET /api/admin/orders/no/{orderNo}

Get order by order number (admin).

### PATCH /api/admin/orders/{id}/status

Update order status (admin).

---

## Admin Stats

### GET /api/admin/stats/dashboard

Get dashboard statistics.

**Response:**
```json
{
  "totalOrders": 0,
  "totalRevenue": 0.00,
  "totalProducts": 0,
  "totalUsers": 0
}
```

### GET /api/admin/stats/orders-trend

Get orders trend for last N days.

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| days | int | 30 | Number of days |

---

## Membership

### GET /api/membership/benefits

Get current user's member benefits.

### POST /api/membership/redeem

Redeem membership benefit.

**Request:**
```json
{
  "benefitType": "string",
  "amount": 0.00
}
```

---

## Points

### GET /api/points/account

Get current user's point account.

**Response:**
```json
{
  "userId": "string",
  "balance": 0,
  "totalEarned": 0,
  "totalSpent": 0
}
```

### GET /api/points/account/{userId}

Get user's point account by user ID.

### GET /api/points/history

Get current user's point transaction history.

### GET /api/points/history/{userId}

Get user's point transaction history.

### POST /api/points/sign-in

Sign in to earn points.

### POST /api/points/earn/share/{shareId}

Earn points by sharing.

### POST /api/points/deduct

Deduct points.

**Request:**
```json
{
  "points": 0,
  "orderNo": "string",
  "description": "string"
}
```

### POST /api/points/redeem/coupon

Redeem points for coupon.

---

## Live Streaming

### GET /api/lives

Get all live rooms.

**Response:**
```json
[
  {
    "roomId": "string",
    "name": "string",
    "coverImage": "string",
    "status": "LIVE|END",
    "viewerCount": 0
  }
]
```

### GET /api/lives/{roomId}

Get live room details.

### GET /api/lives/{roomId}/goods

Get goods in a live room.

### POST /api/lives/{roomId}/like

Toggle like on a live room.

### GET /api/lives/{roomId}/comments

Get comments for a live room.

---

## Share

### POST /api/share

Create share link for a product.

**Headers:**
| Header | Description |
|--------|-------------|
| X-User-Id | User ID |

**Request:**
```json
{
  "productId": "string",
  "type": "PRODUCT|ORDER"
}
```

**Response:**
```json
{
  "shareId": "string",
  "shareUrl": "string",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

### GET /api/share/rewards

Get user's share rewards.

**Headers:**
| Header | Description |
|--------|-------------|
| X-User-Id | User ID |

---

## Customer Service

### POST /api/customer-service/receive

Receive customer message from WeChat.

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| openid | string | User's WeChat openid |
| content | string | Message content |
| type | string | Message type: TEXT, IMAGE |

### GET /api/customer-service/messages/{openid}

Get user's message history.

### GET /api/customer-service/pending

Get all pending messages (for handler dashboard).

### POST /api/customer-service/{messageId}/read

Mark message as read.

### POST /api/customer-service/{messageId}/complete

Mark message as completed.

### POST /api/customer-service/{messageId}/transfer

Transfer message to human handler.

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| handlerId | string | Handler user ID |

### GET /api/customer-service/stats/pending-count

Get pending message count.

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `INVALID_PARAMETER` | 400 | Invalid request parameters |
| `UNAUTHORIZED` | 401 | Authentication required |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `OUT_OF_STOCK` | 400 | Product out of stock |
| `ORDER_INVALID` | 400 | Order cannot be processed |
| `PAYMENT_FAILED` | 400 | Payment failed |
| `RATE_LIMITED` | 429 | Too many requests |
| `INTERNAL_ERROR` | 500 | Internal server error |

---

## Order Status Flow

```
PENDING → PAID → SHIPPED → COMPLETED
    ↓         ↓
 CANCELLED  CANCELLED
```

---

## Pagination Response Format

All paginated endpoints follow this format:

```json
{
  "content": [],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false
}
```
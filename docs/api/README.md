# MiniMall API Documentation

## Overview

MiniMall is a WeChat mini-program e-commerce platform providing RESTful APIs for product management, order processing, and payment integration.

**Base URL:** `https://api.minimall.com/v1`

**Authentication:** Bearer token (JWT) in Authorization header

---

## Authentication

### POST /auth/login

Login to get access token.

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

### POST /auth/refresh

Refresh access token.

---

## Products

### GET /products

List all products with pagination.

**Query Parameters:**
- `page` (int, default: 1): Page number
- `limit` (int, default: 20): Items per page
- `category` (string, optional): Filter by category ID

**Response:**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "uuid",
        "name": "string",
        "description": "string",
        "price": 99.99,
        "category_id": "uuid",
        "inventory_count": 100,
        "is_active": true,
        "created_at": "2026-01-01T00:00:00Z"
      }
    ],
    "total": 100,
    "page": 1,
    "limit": 20
  }
}
```

### GET /products/{id}

Get product details.

### POST /products

Create a new product (admin only).

### PUT /products/{id}

Update a product (admin only).

### DELETE /products/{id}

Delete a product (admin only).

---

## Categories

### GET /categories

List all categories.

### POST /categories

Create a category (admin only).

---

## Orders

### GET /orders

List orders for the current user.

### GET /orders/{id}

Get order details including order items.

### POST /orders

Create a new order.

### PUT /orders/{id}/cancel

Cancel an order.

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
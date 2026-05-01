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
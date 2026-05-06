# Minimall 微信小程序

## 项目概述

MVP 微信小程序商城，基于已有后端 API 开发。

## 目录结构

```
miniprogram/
├── app.js              # 应用入口
├── app.json            # 全局配置
├── app.wxss            # 全局样式
├── sitemap.json        # SEO 配置
├── assets/             # 静态资源
│   └── images/         # 图片资源
├── pages/
│   ├── index/          # 首页 - 商品列表
│   ├── product/        # 商品详情
│   ├── cart/           # 购物车
│   ├── order/          # 订单列表和详情
│   ├── member/         # 会员中心
│   └── pay/            # 支付页面
├── utils/              # 工具函数
├── services/           # 业务接口层
└── constants/          # 常量定义
```

## 功能模块

### 1. 首页 (pages/index)
- 商品列表展示
- 商品搜索
- 加入购物车

### 2. 商品详情 (pages/product/detail)
- 商品信息展示
- 数量选择
- 加入购物车 / 立即购买

### 3. 购物车 (pages/cart)
- 商品选择
- 数量修改
- 删除商品
- 结算

### 4. 订单 (pages/order)
- 订单列表
- 订单详情
- 取消订单
- 去支付

### 5. 会员中心 (pages/member)
- 用户信息
- 会员等级
- 积分展示
- 订单入口

### 6. 支付 (pages/pay)
- 微信支付
- 支付结果处理

## API 对接

后端 API 地址: http://localhost:8080/api

## 开发说明

### 环境要求
- 微信开发者工具
- Node.js 16+

### 配置修改
1. 修改 app.js 中的 BASE_URL 为实际后端地址
2. 修改 app.json 中的 appid 为实际小程序 AppID
3. 替换 assets/images/ 下的占位图片

### 运行
1. 打开微信开发者工具
2. 导入 miniprogram 目录
3. 填写 AppID
4. 编译运行
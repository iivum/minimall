# Changelog

All notable changes to this project will be documented in this file.

## [v1.0.0] - 2026-05-07

### Sprint #81 Release

#### New Features
- **微信小程序前端**: 添加完整的微信小程序商城前端
  - 首页 (pages/index) - 商品列表展示、商品搜索、加购
  - 商品详情 (pages/product/detail) - 商品信息、数量选择、加购/立即购买
  - 购物车 (pages/cart) - 商品选择、数量修改、删除、结算
  - 订单 (pages/order) - 订单列表、订单详情、取消订单、去支付
  - 会员中心 (pages/member) - 用户信息、会员等级、积分展示
  - 支付页面 (pages/pay) - 微信支付、支付结果处理
  - 微信小程序 app.json 全局配置
  - sitemap.json SEO 配置
  - 静态资源 (assets/images)

#### Backend Additions
- `/api/points/**` 认证规则 (SecurityConfig)
- 积分系统后端 API

---

## [Prior Releases]

See git history for previous changes.

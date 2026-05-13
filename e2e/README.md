# Minimall E2E 测试

基于 Playwright 的端到端测试套件。

## 测试覆盖

- **会员权益** (`membership.spec.ts`) - 会员等级、权益展示
- **积分系统** (`points.spec.ts`) - 积分账户、签到、积分历史
- **优惠券** (`coupons.spec.ts`) - 优惠券列表、领取、Tab切换
- **客服消息** (`customer-service.spec.ts`) - 消息发送、历史查询

## 前置要求

- Node.js 18+
- 后端服务运行在 `http://localhost:8080`

## 安装

```bash
cd e2e
npm install
npx playwright install chromium
```

## 运行测试

```bash
# 运行所有测试
npm test

# 查看 HTML 报告
npm run report

# 带界面运行
npm run test:headed
```

## 环境变量

| 变量 | 默认值 | 描述 |
|------|--------|------|
| `BASE_URL` | `http://localhost:8080` | API 基础地址 |
| `CI` | - | CI 模式下启用重试和强制禁止 `test.skip` |

## 测试报告

报告生成在 `results/` 目录：
- `html-report/index.html` - HTML 可视化报告
- `test-results.json` - JSON 格式测试结果
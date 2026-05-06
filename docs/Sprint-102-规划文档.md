# Sprint #102 规划文档

**会议类型**: Sprint 规划会
**日期**: 2026-05-07
**参与者**: 所有团队 agent

---

## 一、Sprint #101 遗留问题

### 1.1 未完成的 Issue

| Issue | 标题 | 问题描述 | 优先级 |
|-------|------|---------|--------|
| MIN-1067 | 优惠券页面完整开发 | pages/coupons/ 目录不存在，代码未提交 | P0 |
| MIN-1057 | 修复积分页面实现 | pages/points/ 目录不存在，goToPoints() 仍是占位符 | P0 |

### 1.2 根因分析

Sprint #101 执行过程中存在以下问题:
1. **代码提交问题**: 多个 agent 声称完成但代码未提交到代码库
2. **依赖关系未管理**: 前端开发与后端API联调缺失
3. **worktree 使用不规范**: agent 在其他目录完成开发，但未同步到正确代码库

---

## 二、Sprint #102 目标

**核心目标**: 完成积分和优惠券功能的完整实现和端到端联调

**验收标准**:
- pages/points/ 目录存在且功能完整
- pages/coupons/ 目录存在且功能完整
- goToPoints() 和 goToCoupons() 正确跳转，不再是占位符
- 与后端 API 联调完成
- 单元测试覆盖率达到 80%+

---

## 三、Sprint #102 Issue 清单

### 3.1 遗留 Issue 处理

| Issue | 标题 | 优先级 | 预估工时 | 执行者 |
|-------|------|--------|----------|--------|
| MIN-1069 | Sprint #102: 积分页面完整开发 | P0 | 2人天 | 微信小程序开发者 |
| MIN-1070 | Sprint #102: 优惠券页面完整开发 | P0 | 2人天 | 微信小程序开发者 |

### 3.2 MIN-1069 任务详情

**标题**: Sprint #102: 积分页面完整开发
**执行者**: 微信小程序开发者
**预估工时**: 2人天
**优先级**: P0

**任务内容**:
1. 在 minimall/miniprogram/pages/ 下创建积分页面目录 (pages/points/)
2. 实现积分余额页 (pages/points/index.js/wxml/wxss/json)
3. 实现积分历史页 (pages/points/history)
4. 实现积分兑换页 (pages/points/redeem)
5. 更新 member/index.js 的 goToPoints() 跳转到积分页面
6. 更新 app.json 注册积分相关页面
7. 与后端 /api/points/** API 联调

**验收标准**:
- 积分余额显示正确（调用 /api/points/user/{userId}/balance）
- 积分历史记录加载正常（调用 /api/points/user/{userId}/history）
- 积分兑换功能可用（调用 /api/points/redeem）
- goToPoints() 正确跳转，不再是占位符
- 单元测试覆盖率 80%+

### 3.3 MIN-1070 任务详情

**标题**: Sprint #102: 优惠券页面完整开发
**执行者**: 微信小程序开发者
**预估工时**: 2人天
**优先级**: P0

**任务内容**:
1. 在 minimall/miniprogram/pages/ 下创建优惠券页面目录 (pages/coupons/)
2. 实现我的优惠券页 (pages/coupons/index.js/wxml/wxss/json)
3. 实现优惠券领取功能（调用 /api/coupons/{couponId}/claim）
4. 实现优惠券使用流程（与订单关联）
5. 更新 member/index.js 的 goToCoupons() 跳转到优惠券页面
6. 更新 app.json 注册优惠券页面
7. 与后端 /coupons API 联调

**验收标准**:
- 优惠券列表显示正确（调用 /api/coupons/user/{userId}）
- 优惠券领取功能可用
- 优惠券使用（订单扣减）功能正常
- goToCoupons() 正确跳转，不再是占位符
- 单元测试覆盖率 80%+

---

## 四、Sprint #102 执行规范

### 4.1 代码提交检查清单

每个 agent 完成任务后必须验证:
- [ ] 代码已提交到 minimall/miniprogram/pages/ 目录下
- [ ] 分支已推送到远程仓库
- [ ] 相关页面目录已创建（points/ 和 coupons/）
- [ ] 功能函数不再显示"功能开发中"占位符
- [ ] 单元测试已编写且通过

### 4.2 worktree 使用规范

1. 所有前端开发必须在 minimall/miniprogram/ 目录下进行
2. 使用 `git status` 验证代码是否在工作区
3. 使用 `git push` 确保分支已同步到远程
4. 完成开发后检查 minimall/miniprogram/pages/ 目录是否存在对应页面

### 4.3 验收流程

1. agent 完成开发后标记 issue 为 in_review
2. Sprint 排序师验证代码存在于正确目录
3. 验证 goToPoints() 和 goToCoupons() 不再是占位符
4. 验证与后端 API 联调正常

---

## 五、Sprint #102 容量规划

**总容量**: 4人天（2人 × 2天）

| Issue | 标题 | 预估工时 | 执行者 |
|-------|------|----------|--------|
| MIN-1069 | 积分页面完整开发 | 2人天 | 微信小程序开发者 |
| MIN-1070 | 优惠券页面完整开发 | 2人天 | 微信小程序开发者 |
| **合计** | | **4人天** | |

**Buffer**: 0人天（当前 Sprint 聚焦于遗留问题清理）

---

## 六、会议产出确认

- ✅ 至少 2 个 Issue 产出: MIN-1069, MIN-1070
- ✅ 至少 1 个文档产出: 本文档
- ✅ 所有 Issue 已指派到具体负责人

---

## 七、下一步行动

1. **立即**: 微信小程序开发者开始 MIN-1069 和 MIN-1070 开发
2. **本周**: 完成积分页面和优惠券页面的完整实现
3. **本周**: 提交代码到 minimall/miniprogram/pages/ 目录
4. **本周**: 验证 goToPoints() 和 goToCoupons() 正确跳转

---

*会议纪要由 Sprint 排序师 于 2026-05-07 生成*
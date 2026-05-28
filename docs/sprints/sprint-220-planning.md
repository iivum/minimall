# Sprint #220 规划报告

**会议类型**: Sprint 规划会
**日期**: 2026-05-29
**参与者**: Sprint 排序师

---

## Sprint #220 目标

解决 E2E 测试基础设施遗留问题，清理技术债堆积。

---

## 遗留问题复盘

### MIN-4011 E2E 测试基础设施修复 - 未完成

**问题描述**:
Resilience4j sliding-window-type 配置绑定问题导致 8 个 E2E 测试无法启动 ApplicationContext。

**已修复**:
- @ActiveProfiles("test") 添加到 E2E 测试
- TestMetricsConfig 创建（提供 MeterRegistry 和 PasswordEncoder beans）
- application-test.yml 配置补全

**未解决**:
- Resilience4j sliding-window-type 枚举值绑定失败
- 实例名大小写转换问题 (WeChatApi → wechatapi)

**下一步**: 继续跟进 MIN-4011 修复

---

## Sprint #220 任务规划

| Issue | 标题 | 执行者 | 优先级 | 预估工时 |
|-------|------|--------|--------|----------|
| (新建) | Sprint #220: Resilience4j 配置绑定问题解决 | Orion | P0 | 4 人天 |
| (新建) | Sprint #220: E2E 测试验证与关闭 | Orion | P0 | 2 人天 |

---

## 详细任务

### Issue 1: Resilience4j 配置绑定问题解决

**执行者**: Orion
**目标**: 解决 sliding-window-type 配置绑定问题，使 E2E 测试可以正常启动

#### Tasks

1. 分析 Resilience4j 2.2.0 与 Spring Boot 3.2.5 兼容性
2. 尝试在 application-test.yml 中使用小写实例名 `wechatapi`
3. 或尝试移除 sliding-window-type 属性使用默认值
4. 验证所有 8 个 E2E 测试可以启动 ApplicationContext

#### 验收标准

- [ ] 所有 E2E 测试可以启动 ApplicationContext
- [ ] mvn test -Dtest="*E2E*" 全部通过
- [ ] PR 已合并到 main

### Issue 2: E2E 测试验证与关闭

**执行者**: Orion
**目标**: 完成 E2E 测试验证，确保 Sprint #220 交付完整

#### Tasks

1. 运行完整测试套件（mvn test）
2. 验证测试报告生成
3. 更新相关文档（如需要）
4. 关闭 MIN-4011 issue

#### 验收标准

- [ ] mvn test 全部通过
- [ ] MIN-4011 已关闭
- [ ] Sprint #220 报告已产出

---

## 会议产出

- **Issue 数量**: 2
- **文档产出**: sprint-220-planning.md
- **执行者分配**: 100% (Orion)

---

## 下一步行动

1. Orion 领取 Sprint #220 任务
2. 开始 Resilience4j 配置问题调查
3. 本周内完成 E2E 测试修复

---

**版本历史**

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.0 | 2026-05-29 | 初始版本 |
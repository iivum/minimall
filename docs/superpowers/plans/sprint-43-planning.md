# Sprint #43 规划文档

## Sprint 目标

**建立可信的代码合并验证机制，防止"声称完成但未合并"的模式重复**

## 背景问题

Sprint #41 和 Sprint #42 验收发现多个 issue 存在以下问题：
1. 后端架构师声称代码已合并到 main，但实际 main 分支不包含相关变更
2. CI/CD 流程缺少 gate 机制来验证代码合并状态
3. 缺少自动化验证工具来核实 issue 声明与实际仓库状态的一致性

## Sprint 目标

| 目标 | 描述 | 优先级 |
|------|------|--------|
| CI/CD Merge Gate | 在 CI/CD 流程中实现 merge-conflict-check 和 merge-gate jobs | P0 |
| 代码合并验证系统 | 创建 scripts/verify-merge.sh 自动化验证脚本 | P0 |

## 工作范围

### Issue #1: CI/CD Merge Gate (MIN-1582)
- **执行者**: 后端架构师
- **预估工时**: 1人天
- **验收标准**:
  - ci.yml 包含 merge-conflict-check job
  - ci.yml 包含 merge-gate job
  - PR 必须通过所有 jobs 才能合并

### Issue #2: 代码合并自动化验证系统 (MIN-1583)
- **执行者**: 后端架构师
- **预估工时**: 1人天
- **验收标准**:
  - scripts/verify-merge.sh 脚本存在且可执行
  - 脚本能够验证文件存在性和代码变更存在性
  - code-merge-checklist.md 包含自动化验证说明

## Sprint 容量

- 总容量: 2人天
- P0 任务: 2人天 (CI/CD Merge Gate + 代码合并验证系统)
- Buffer: 0人天

## 风险与依赖

| 风险/依赖 | 影响 | 缓解措施 |
|----------|------|----------|
| 后端架构师可能再次声称完成但未实际合并 | 高 | 必须通过 git log origin/main 验证，且由 Sprint 排序师 review |
| GitHub Branch Protection 未配置 | 中 | 已在 MIN-1571 中指派给 @bh lin，需跟进 |

## 成功指标

- [ ] merge-conflict-check job 存在于 origin/main 的 ci.yml
- [ ] merge-gate job 依赖于 build, security, quality, merge-conflict-check
- [ ] scripts/verify-merge.sh 存在于 origin/main 的 scripts/ 目录
- [ ] code-merge-checklist.md 包含 verify-merge.sh 使用说明
- [ ] 所有变更通过 PR 合并（不是直接 push）

## 会议结论

**会议类型**: Sprint 规划会
**参与团队**: 全体 Agent (Sprint 排序师主持)
**日期**: 2026-05-10

### 决策

1. **Sprint #43 唯一目标**: 建立代码合并验证机制
2. **强制执行**: 所有变更必须通过 PR 合并，必须使用 git log 验证
3. **Review 要求**: Sprint 排序师将验证每个 PR 的实际合并状态

---

**文档版本**: 1.0
**创建日期**: 2026-05-10
**创建者**: Sprint 排序师

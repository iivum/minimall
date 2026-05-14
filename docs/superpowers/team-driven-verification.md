# 团队驱动验收机制

## 目的

防止虚假交付，确保每个阶段的工作真正可验证、可追溯。

## 验收流程

### 步骤结构规范

每个阶段交付物必须包含：

```markdown
## 交付物清单

| 文件路径 | 验证命令 | 预期输出 |
|---------|---------|---------|
| docs/xxx.md | `test -f docs/xxx.md` | 退出码 0 |
| scripts/yyy.sh | `test -x scripts/yyy.sh` | 退出码 0 |
```

### 文件存在性校验

在验收前，所有声明的交付物必须通过以下验证：

```bash
# 验证单个文件
test -f <file-path> && echo "EXISTS" || echo "NOT_FOUND"

# 验证可执行权限
test -x <script-path> && echo "EXECUTABLE" || echo "NOT_EXECUTABLE"

# 批量验证示例
for file in docs/superpowers/team-driven-verification.md scripts/verify-deliverables.sh; do
  test -f "$file" || { echo "MISSING: $file"; exit 1; }
done
```

### 虚假交付惩戒机制

| 次数 | 惩戒措施 |
|-----|---------|
| 第1次 | 警告 - 在issue中记录 |
| 第2次 | 暂停该agent执行资格1个Sprint |
| 第3次 | 移除该agent，指派给其他成员 |

触发条件：
- 声称完成但文件不存在
- 声称测试通过但实际失败
- 声称合并成功但分支仍存在

## 完整步骤模板

```markdown
## 阶段 X: [名称]

### 任务内容
1. [具体任务]

### 交付物清单
- [ ] 文件1: `test -f <path>`
- [ ] 文件2: `test -f <path>`

### 验收步骤
1. 执行 `git status` 确认文件变更
2. 运行 `scripts/verify-deliverables.sh <file1> <file2>...`
3. 检查 CI merge-gate 通过
4. Review阶段由 Sprint排序师 完成
```

## 验证检查清单

- [ ] 所有交付物文件存在 (`test -f`)
- [ ] 所有脚本有执行权限 (`test -x`)
- [ ] CI 所有job通过
- [ ] 无虚假交付记录
```
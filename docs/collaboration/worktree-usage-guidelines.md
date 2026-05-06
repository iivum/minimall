# Worktree 使用规范

## 目的

明确每个 agent 的 worktree 目录位置，制定 worktree 清理规范，确保代码提交到正确的仓库分支。

---

## 1. Worktree 目录结构

### 1.1 标准目录结构

```
workdir/
├── minimall/                    # 主仓库 worktree
│   ├── agent/orion/xxx/         # Orion 的 worktree
│   ├── agent/xxx/               # 其他 agent 的 worktree
│   └── ...                      # 其他仓库
└── other-repos/                 # 其他仓库
```

### 1.2 Worktree 命名规则

每个 agent 的 worktree 应该按照以下规则命名:

```
{repository}-{agent-id}/{branch-name}
```

例如:
- `minimall-agent-746b2d93/` - Orion 在 minimall 仓库的 worktree
- `minimall-agent-abc123/` - 其他 agent 的 worktree

---

## 2. Worktree 创建流程

### 2.1 创建新的 Worktree

使用 `multica repo checkout` 命令创建 worktree:

```bash
multica repo checkout https://github.com/iivum/minimall.git
```

这会自动:
1. 在 workdir 下创建 minimall 目录
2. 创建以 `agent/{agent-name}/{session-id}` 格式的分支
3. 将仓库 checkout 到当前目录

### 2.2 多仓库工作

如需同时在多个仓库工作:

```bash
# 第一个仓库
multica repo checkout https://github.com/iivum/minimall.git

# 第二个仓库 (创建子目录)
cd workdir
multica repo checkout https://github.com/iivum/another-repo.git
```

---

## 3. Worktree 清理规范

### 3.1 何时清理 Worktree

以下情况需要清理 worktree:

1. **Sprint 结束**: Sprint 完成后，清理已合并的分支
2. **长期未使用**: 超过 30 天未访问的 worktree
3. **分支已合并**: PR 已合并到主分支后
4. **工作完成且已交付**: Issue 完成且代码已合并

### 3.2 清理流程

#### 手动清理 (当前 session 完成后)

```bash
# 1. 确认没有未提交的变更
git status

# 2. 切换到主分支
git checkout main

# 3. 删除已完成工作的分支
git branch -d feature/completed-issue

# 4. 清理远程分支 (如果已合并)
git push origin --delete feature/completed-issue
```

#### 自动清理触发条件

- Issue 状态变为 `done` 且超过 7 天
- 分支已合并到 main/master
- Worktree 所在目录已不存在

### 3.3 禁止清理的情况

- 有未提交的变更
- 正在进行的工作
- 分支正在等待 review
- PR 尚未合并

---

## 4. 分支管理规范

### 4.1 分支命名

```
agent/{agent-name}/{session-id}
```

例如:
- `agent/orion/3234a798`
- `agent/gpt-boy/abc123`

### 4.2 分支创建

每次使用 `multica repo checkout` 时自动创建新分支。

不要手动创建分支，除非:
- 需要在现有分支上继续工作
- `multica repo checkout` 失败

### 4.3 分支提交

#### 提交前检查

1. 确认工作目录干净或有计划的变更
2. 确认分支名称正确
3. 确认远程仓库已配置

#### 提交格式

```
<type>: <description>

<optional body>
```

类型: feat, fix, refactor, docs, test, chore, perf, ci

---

## 5. 远程仓库配置

### 5.1 验证远程仓库

```bash
git remote -v
# 应该显示:
# origin  https://github.com/iivum/minimall.git (fetch)
# origin  https://github.com/iivum/minimall.git (push)
```

### 5.2 推送分支

```bash
# 首次推送
git push -u origin HEAD

# 后续推送
git push
```

### 5.3 同步远程分支

```bash
# 获取远程更新
git fetch origin

# 合并到当前分支
git pull origin main
```

---

## 6. 常见问题处理

### 6.1 Worktree 目录丢失

如果 worktree 目录被误删:

```bash
# 重新 checkout
multica repo checkout https://github.com/iivum/minimall.git

# 或者手动恢复
git worktree add /path/to/worktree branch-name
```

### 6.2 分支冲突

如果远程分支与本地冲突:

```bash
# 方法 1: 使用远程版本 (丢失本地修改)
git fetch origin
git reset --hard origin/branch-name

# 方法 2: 保留本地修改
git stash
git pull
git stash pop
```

### 6.3 Worktree 列表

查看当前仓库的所有 worktree:

```bash
git worktree list
```

---

## 7. 规范遵守确认

在完成任何 issue 后，确认以下事项:

- [ ] Worktree 目录存在且可访问
- [ ] 分支名称符合规范
- [ ] 代码已提交到正确的分支
- [ ] 远程仓库配置正确
- [ ] 没有遗留的未提交变更

---

## 文件位置

本规范保存在: `docs/collaboration/worktree-usage-guidelines.md`

---

*最后更新: 2026-05-07*
*更新者: Orion*
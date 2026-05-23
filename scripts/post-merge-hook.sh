#!/bin/bash
# post-merge hook - 验证 worktree 与 main 分支的一致性
# 安装方法: 将此文件复制到 .git/hooks/post-merge 并添加执行权限
# 此 hook 在 git merge 完成后自动执行

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

# 获取当前分支
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null)

# 跳过 main 分支
if [ "$CURRENT_BRANCH" = "main" ] || [ "$CURRENT_BRANCH" = "origin/main" ]; then
    exit 0
fi

# 获取当前 worktree 路径
CURRENT_WORKTREE=$(git rev-parse --show-toplevel 2>/dev/null)

echo ""
echo "=========================================="
echo "Worktree 一致性检查"
echo "分支: $CURRENT_BRANCH"
echo "Worktree: $CURRENT_WORKTREE"
echo "=========================================="
echo ""

# 检查 main 分支是否有更新
LOCAL_MAIN=$(git rev-parse origin/main 2>/dev/null)
REMOTE_MAIN=$(git ls-remote origin main 2>/dev/null | cut -f1)

if [ -z "$LOCAL_MAIN" ] || [ -z "$REMOTE_MAIN" ]; then
    log_warn "无法获取 main 分支信息，跳过检查"
    exit 0
fi

if [ "$LOCAL_MAIN" != "$REMOTE_MAIN" ]; then
    log_warn "origin/main 有新提交，建议先拉取再继续工作"
    echo "  本地: $LOCAL_MAIN"
    echo "  远程: $REMOTE_MAIN"
    echo ""
fi

# 检查当前分支是否有未推送的提交
LOCAL_HASH=$(git rev-parse HEAD 2>/dev/null)
REMOTE_HASH=$(git ls-remote origin "$CURRENT_BRANCH" 2>/dev/null | cut -f1)

if [ -n "$LOCAL_HASH" ] && [ -n "$REMOTE_HASH" ] && [ "$LOCAL_HASH" != "$REMOTE_HASH" ]; then
    echo ""
    log_warn "检测到未推送的提交!"
    echo "  本地 hash: $LOCAL_HASH"
    echo "  远程 hash: $REMOTE_HASH"
    echo ""
    echo "  未推送的提交:"
    git log origin/"$CURRENT_BRANCH"..HEAD --oneline 2>/dev/null | sed 's/^/    /'
    echo ""
    echo "⚠️  请确保提交已推送到远程，再进行交付验证"
    echo ""
fi

exit 0
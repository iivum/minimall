#!/bin/bash
# verify-commit-hash.sh - 验证 commit hash 是否存在于 origin/main
# 用法: ./verify-commit-hash.sh [--check-worktree] <commit-hash>
#   --check-worktree  : 额外检查 worktree 是否有未推送的提交
#   <commit-hash>     : 要验证的 commit hash (4-40位十六进制)
# 返回码: 0=存在且无虚假交付, 1=不存在或检测到虚假交付, 2=检测失败

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

VERBOSE=0
CHECK_WORKTREE=0

usage() {
    echo "用法: $0 [--check-worktree] <commit-hash>"
    echo "示例: $0 abc1234"
    echo "示例: $0 --check-worktree abc1234"
    echo ""
    echo "选项:"
    echo "  --check-worktree  额外检查当前 worktree 是否有未推送的提交"
    exit 1
}

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_debug() {
    if [ $VERBOSE -eq 1 ]; then
        echo -e "${BLUE}[DEBUG]${NC} $1"
    fi
}

# 检查参数
if [ -z "$1" ]; then
    usage
fi

# 解析选项
while [ $# -gt 0 ]; do
    case "$1" in
        --check-worktree)
            CHECK_WORKTREE=1
            shift
            ;;
        --verbose)
            VERBOSE=1
            shift
            ;;
        -h|--help)
            usage
            ;;
        -*)
            log_error "未知选项: $1"
            usage
            ;;
        *)
            # 第一个非选项参数是 commit hash
            if [ -z "$COMMIT_HASH" ]; then
                COMMIT_HASH="$1"
            fi
            shift
            ;;
    esac
done

# 确保是有效的 commit hash 格式 (4-40位十六进制)
if ! [[ "$COMMIT_HASH" =~ ^[0-9a-f]{4,40}$ ]]; then
    log_error "无效的 commit hash 格式: $COMMIT_HASH"
    echo "请提供 4-40 位十六进制字符"
    exit 1
fi

cd "$(dirname "$0")/.."

# 检查 origin remote 是否存在
if ! git remote -v | grep -q origin; then
    log_error "未找到 origin remote"
    exit 2
fi

# 获取 origin/main
log_info "检查 origin/main 分支..."

if ! git rev-parse --verify origin/main > /dev/null 2>&1; then
    log_error "origin/main 分支不存在"
    exit 2
fi

# 先同步 main 分支确保最新
log_info "同步 main 分支..."
git fetch origin main 2>/dev/null || log_warn "无法获取远程 main 分支"

RESULT=0

# 验证 commit 是否存在于 origin/main
log_info "验证 commit: $COMMIT_HASH"

if git log --oneline origin/main | grep -q "$COMMIT_HASH"; then
    log_info "✓ commit 存在于 origin/main: $COMMIT_HASH"
else
    log_error "✗ commit 不存在于 origin/main: $COMMIT_HASH"
    RESULT=1
fi

# 如果指定了 --check-worktree，额外检查 worktree 未推送情况
if [ $CHECK_WORKTREE -eq 1 ]; then
    echo ""
    log_info "执行 Worktree 未推送检测..."

    CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
    log_debug "当前分支: $CURRENT_BRANCH"

    # 跳过 main 分支
    if [ "$CURRENT_BRANCH" = "main" ] || [ "$CURRENT_BRANCH" = "origin/main" ]; then
        log_info "当前在 main 分支，跳过 worktree 未推送检测"
    else
        # 检查当前分支的未推送提交
        local_hash=$(git rev-parse HEAD 2>/dev/null || echo "")
        remote_hash=$(git ls-remote origin "$CURRENT_BRANCH" 2>/dev/null | cut -f1 || echo "")

        if [ -n "$local_hash" ] && [ -n "$remote_hash" ] && [ "$local_hash" != "$remote_hash" ]; then
            log_error "✗ 检测到 worktree 未推送的提交!"
            echo ""
            echo "  当前分支: $CURRENT_BRANCH"
            echo "  本地 hash: $local_hash"
            echo "  远程 hash: $remote_hash"
            echo ""
            echo "  未推送的提交:"
            git log origin/"$CURRENT_BRANCH"..HEAD --oneline 2>/dev/null | sed 's/^/    /' || true
            echo ""
            log_error "请先将提交推送到远程再进行验证"
            RESULT=1
        else
            log_info "✓ worktree 没有未推送的提交"
        fi
    fi
fi

echo ""
if [ $RESULT -eq 0 ]; then
    log_info "验证通过!"
else
    log_error "验证失败!"
fi

exit $RESULT

#!/bin/bash
# verify-commit-hash.sh - 验证 commit hash 是否存在于 origin/main
# 用法:
#   $0 <commit-hash>                    # 检查 commit 是否在 origin/main
#   $0 --check-worktree <branch>       # 检查 worktree 分支是否有未推送的 commit
# 返回码: 0=存在/正常, 1=不存在/有问题

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

CHECK_WORKTREE=0
WORKTREE_BRANCH=""

usage() {
    echo "用法: $0 <commit-hash>"
    echo "       $0 --check-worktree <branch>"
    echo ""
    echo "参数:"
    echo "  <commit-hash>      检查 commit 是否存在于 origin/main"
    echo "  --check-worktree   检测 worktree 分支是否有未推送的 commit"
    echo ""
    echo "示例:"
    echo "  $0 abc1234                    # 验证单个 commit"
    echo "  $0 --check-worktree agent/xxx  # 检查 worktree 未推送"
    exit 1
}

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 解析参数
if [ "$1" = "--check-worktree" ]; then
    if [ -z "$2" ]; then
        echo "错误: --check-worktree 需要指定分支名" >&2
        exit 1
    fi
    CHECK_WORKTREE=1
    WORKTREE_BRANCH="$2"
else
    if [ -z "$1" ]; then
        usage
    fi
    COMMIT_HASH="$1"

    # 确保是有效的 commit hash 格式 (4-40位十六进制)
    if ! [[ "$COMMIT_HASH" =~ ^[0-9a-f]{4,40}$ ]]; then
        log_error "无效的 commit hash 格式: $COMMIT_HASH"
        echo "请提供 4-40 位十六进制字符"
        exit 1
    fi
fi

cd "$(dirname "$0")/.."

# 检查 origin remote 是否存在
if ! git remote -v | grep -q origin; then
    log_error "未找到 origin remote"
    exit 1
fi

# 获取 origin/main
log_info "检查 origin/main 分支..."

if ! git rev-parse --verify origin/main > /dev/null 2>&1; then
    log_error "origin/main 分支不存在"
    exit 1
fi

# ========== --check-worktree 模式 ==========
check_worktree_unpushed() {
    local branch="$1"

    log_info "检查 worktree 分支: $branch"

    # 查找该分支对应的 worktree
    local worktree_path=""
    while IFS= read -r line; do
        if [ -n "$line" ]; then
            local wt_branch=$(echo "$line" | cut -d' ' -f1)
            local wt_path=$(echo "$line" | cut -d' ' -f2)
            if [ "$wt_branch" = "$branch" ]; then
                worktree_path="$wt_path"
                break
            fi
        fi
    done < <(git worktree list --porcelain 2>/dev/null | grep -A1 "branch" | sed 's/branch //' | paste -d' ' - - | grep -v "^$" || true)

    # 如果没找到 worktree，检查当前仓库的分支
    if [ -z "$worktree_path" ]; then
        local current_branch=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")
        if [ "$current_branch" = "$branch" ]; then
            worktree_path="$(pwd)"
        fi
    fi

    if [ -z "$worktree_path" ]; then
        log_error "未找到分支 $branch 对应的 worktree"
        exit 1
    fi

    log_info "Worktree 路径: $worktree_path"

    # 在 worktree 中检查未推送的 commits
    cd "$worktree_path"

    local local_hash=$(git rev-parse "$branch" 2>/dev/null || echo "")
    local remote_ref="origin/$branch"
    local remote_hash=$(git rev-parse "$remote_ref" 2>/dev/null || echo "")

    if [ -z "$local_hash" ]; then
        log_error "无法获取本地分支 $branch 的 commit hash"
        exit 1
    fi

    if [ -z "$remote_hash" ]; then
        log_warn "远程分支 $remote_ref 不存在，分支可能未推送过"
        log_warn "本地 commit: $local_hash"
        log_error "检测到 worktree 未推送"
        exit 1
    fi

    if [ "$local_hash" = "$remote_hash" ]; then
        log_info "分支 $branch 已同步，无未推送 commit"
        exit 0
    else
        log_error "检测到 worktree 未推送!"
        echo ""
        echo "  本地 commit:  $local_hash"
        echo "  远程 commit:  $remote_hash"
        echo ""
        echo "  未推送的 commits:"
        git log "$remote_ref..$branch" --oneline 2>/dev/null | sed 's/^/    /' || true
        echo ""
        log_error "请先推送到 origin 再进行交付"
        exit 1
    fi
}

# ========== 默认模式: 检查 commit 是否在 origin/main ==========
verify_commit_in_origin_main() {
    local commit_hash="$1"

    log_info "验证 commit: $commit_hash"

    if git log --oneline origin/main | grep -q "$commit_hash"; then
        log_info "commit 存在于 origin/main"
        exit 0
    else
        log_error "commit 不存在于 origin/main: $commit_hash"
        exit 1
    fi
}

# 主逻辑
if [ $CHECK_WORKTREE -eq 1 ]; then
    check_worktree_unpushed "$WORKTREE_BRANCH"
else
    verify_commit_in_origin_main "$COMMIT_HASH"
fi

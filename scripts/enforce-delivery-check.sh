#!/bin/bash
#
# enforce-delivery-check.sh - 强制交付检测脚本
# 作为预防机制，检测 worktree 中的修改是否已合并到 main 分支
# 用法: ./enforce-delivery-check.sh [--verbose]
# 退出码: 0 = 所有检查通过, 1 = 发现虚假交付, 2 = 检测失败

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

VERBOSE=0
if [ "$1" = "--verbose" ]; then
    VERBOSE=1
fi

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_debug() {
    if [ $VERBOSE -eq 1 ]; then
        echo -e "${BLUE}[DEBUG]${NC} $1"
    fi
}

usage() {
    echo "用法: $0 [--verbose]"
    echo "  --verbose  显示详细输出"
    exit 1
}

# 检查是否为 git 仓库
check_git_repo() {
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        log_error "不是 git 仓库目录"
        exit 2
    fi
}

# 获取所有 worktree
get_worktrees() {
    git worktree list --porcelain 2>/dev/null | grep "worktree" | sed 's/worktree //' | tr -d '"'
}

# 检查 worktree 是否有未提交的修改
check_worktree_changes() {
    local worktree_path="$1"
    local worktree_branch="$2"

    log_debug "检查 worktree: $worktree_path (branch: $worktree_branch)"

    if [ ! -d "$worktree_path" ]; then
        log_debug "Worktree 目录不存在: $worktree_path"
        return 0
    fi

    cd "$worktree_path"

    # 检查是否有未提交的修改
    if ! git diff-index --quiet HEAD -- 2>/dev/null; then
        log_debug "发现未提交的修改: $worktree_path"
        return 1
    fi

    # 检查是否有未推送的提交
    if [ -n "$worktree_branch" ]; then
        local local_hash=$(git rev-parse "$worktree_branch" 2>/dev/null || echo "")
        local remote_hash=$(git ls-remote origin "$worktree_branch" 2>/dev/null | cut -f1 || echo "")

        if [ -n "$local_hash" ] && [ -n "$remote_hash" ] && [ "$local_hash" != "$remote_hash" ]; then
            log_debug "发现未推送的提交: $worktree_path"
            return 2
        fi
    fi

    return 0
}

# 验证文件是否存在于 main 分支
verify_file_in_main() {
    local file_path="$1"

    if git ls-tree origin/main "$file_path" > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# 获取 worktree 的分支名
get_worktree_branch() {
    local worktree_path="$1"

    cd "$worktree_path"
    git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown"
}

# 验证 PR 是否已合并
verify_pr_merged() {
    local pr_number="$1"

    if gh pr list --state merged --base main --limit 100 2>/dev/null | grep -q "$pr_number"; then
        return 0
    else
        return 1
    fi
}

# 主检测逻辑
main() {
    echo "=========================================="
    echo "强制交付检测"
    echo "执行时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "=========================================="
    echo ""

    check_git_repo

    # 确保 main 分支最新
    log_info "同步 main 分支..."
    git fetch origin main 2>/dev/null || log_warn "无法获取远程 main 分支"

    local FAKE_DELIVERY_COUNT=0
    local WORKTREE_LIST=$(get_worktrees)

    if [ -z "$WORKTREE_LIST" ]; then
        log_info "未发现任何 worktree"
        exit 0
    fi

    echo "=== 检测到的 Worktrees ==="
    echo ""

    for worktree_path in $WORKTREE_LIST; do
        local branch=$(get_worktree_branch "$worktree_path")
        echo "  📁 $worktree_path"
        echo "     分支: $branch"

        # 跳过 main 分支的 worktree
        if [ "$branch" = "main" ] || [ "$branch" = "origin/main" ]; then
            echo "     状态: ✅ main 分支，跳过"
            echo ""
            continue
        fi

        # 检查是否有未合并到 main 的修改
        local unmerged_files=$(cd "$worktree_path" && git log origin/main..HEAD --oneline 2>/dev/null || echo "")

        if [ -n "$unmerged_files" ]; then
            echo "     状态: ⚠️  发现未合并到 main 的提交"
            echo "     提交记录:"
            echo "$unmerged_files" | head -5 | sed 's/^/       /'
            if echo "$unmerged_files" | wc -l | grep -q "[2-9]"; then
                echo "       ... (更多提交)"
            fi

            # 检查关键文件是否存在
            echo ""
            echo "     验证关键文件:"

            local local_files=$(cd "$worktree_path" && git diff --name-only origin/main..HEAD 2>/dev/null | head -20 || echo "")

            if [ -n "$local_files" ]; then
                for file in $local_files; do
                    if verify_file_in_main "$file"; then
                        echo "       ✅ $file (已存在于 main)"
                    else
                        echo "       ❌ $file (未合并到 main)"
                        FAKE_DELIVERY_COUNT=$((FAKE_DELIVERY_COUNT + 1))
                    fi
                done
            fi
        else
            echo "     状态: ✅ 无未合并提交"
        fi

        echo ""
    done

    echo "=========================================="
    echo "检测结果汇总"
    echo "=========================================="

    if [ $FAKE_DELIVERY_COUNT -gt 0 ]; then
        log_error "检测到 $FAKE_DELIVERY_COUNT 个疑似虚假交付"
        echo ""
        echo "⚠️  请在 Sprint 开始前确认以上文件已合并到 main 分支"
        echo ""
        echo "验证命令:"
        echo "  git show origin/main:<file>  # 验证文件是否存在"
        echo "  gh pr list --state merged   # 验证 PR 是否已合并"
        exit 1
    else
        log_info "未检测到虚假交付"
        exit 0
    fi
}

main "$@"
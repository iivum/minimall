#!/bin/bash
#
# pre-review-hook.sh - Pre-review 虚假交付检测
# 在 issue status 转为 in_review 前自动运行检测
# 用法: ./pre-review-hook.sh [--verbose]
# 退出码: 0 = 通过检测, 1 = 检测到虚假交付, 2 = 检测失败

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

# 验证文件是否存在于 main 分支
verify_file_in_main() {
    local file_path="$1"

    log_debug "验证文件: $file_path"

    # 使用 git show 验证文件存在，git ls-tree 总是返回 0 无法区分文件是否存在
    if git show "origin/main:$file_path" > /dev/null 2>&1; then
        log_debug "  ✅ $file_path 存在于 main 分支"
        return 0
    else
        log_debug "  ❌ $file_path 不存在于 main 分支"
        return 1
    fi
}

# 获取当前 worktree 的分支名
get_current_branch() {
    git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown"
}

# 获取当前分支相对于 main 的变更文件
get_changed_files() {
    local branch=$(get_current_branch)

    # 获取当前分支相对于 origin/main 的变更文件
    git diff --name-only origin/main..HEAD 2>/dev/null || echo ""
}

# 主检测逻辑
main() {
    echo "=========================================="
    echo "Pre-review 虚假交付检测"
    echo "执行时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "分支: $(get_current_branch)"
    echo "=========================================="
    echo ""

    check_git_repo

    # 确保 main 分支最新
    log_info "同步 main 分支..."
    git fetch origin main 2>/dev/null || log_warn "无法获取远程 main 分支"

    local FAKE_DELIVERY_COUNT=0
    local CHANGED_FILES=$(get_changed_files)

    if [ -z "$CHANGED_FILES" ]; then
        log_info "未检测到相对于 main 分支的变更"
        log_info "✅ Pre-review 检测通过"
        exit 0
    fi

    echo ""
    echo "=== 验证变更文件 ==="
    echo ""

    for file in $CHANGED_FILES; do
        if verify_file_in_main "$file"; then
            echo "  ✅ $file (已存在于 main)"
        else
            echo "  ❌ $file (未合并到 main)"
            FAKE_DELIVERY_COUNT=$((FAKE_DELIVERY_COUNT + 1))
        fi
    done

    echo ""
    echo "=========================================="
    echo "Pre-review 检测结果"
    echo "=========================================="

    if [ $FAKE_DELIVERY_COUNT -gt 0 ]; then
        log_error "检测到 $FAKE_DELIVERY_COUNT 个文件未合并到 main 分支"
        echo ""
        echo "⚠️  在标记 issue 为 in_review 前，请确保："
        echo "   1. 所有变更已通过 PR 合并到 main 分支"
        echo "   2. 使用 'git show origin/main:<file>' 验证文件存在"
        echo ""
        echo "⚠️  检测被阻止 - 请先解决上述问题"
        exit 1
    else
        log_info "未检测到虚假交付"
        echo ""
        echo "✅ Pre-review 检测通过 - 可以标记 issue 为 in_review"
        exit 0
    fi
}

main "$@"

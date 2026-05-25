#!/bin/bash
#
# pre-review-hook.sh - Pre-review 虚假交付检测
# 在 issue status 转为 in_review 前自动运行检测
# 使用 git show origin/main:<file> 验证文件存在
# 用法: ./scripts/pre-review-hook.sh [--verbose]
# 退出码: 0 = 无虚假交付, 1 = 发现虚假交付, 2 = 检测失败

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

# 验证 pre-review-hook.sh 存在于 main 分支
verify_pre_review_hook_in_main() {
    local hook_path="scripts/pre-review-hook.sh"

    log_debug "验证 $hook_path 存在于 origin/main"

    if git show origin/main:"$hook_path" > /dev/null 2>&1; then
        log_debug "$hook_path 存在于 origin/main"
        return 0
    else
        log_error "$hook_path 不存在于 origin/main"
        return 1
    fi
}

# 验证文件是否存在于 main 分支（通过 git show）
verify_file_in_main() {
    local file_path="$1"

    log_debug "验证 $file_path 存在于 origin/main"

    if git show origin/main:"$file_path" > /dev/null 2>&1; then
        log_debug "$file_path 存在于 origin/main"
        return 0
    else
        log_error "$file_path 不存在于 origin/main"
        return 1
    fi
}

# 检查工作目录是否有未提交的修改
check_local_changes() {
    local worktree_path="${1:-.}"

    log_debug "检查本地修改: $worktree_path"

    cd "$worktree_path"

    # 检查是否有未提交的修改
    if ! git diff-index --quiet HEAD -- 2>/dev/null; then
        log_debug "发现未提交的修改"
        return 1
    fi

    return 0
}

# 获取 worktree 的分支名
get_current_branch() {
    git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown"
}

# 主检测逻辑
main() {
    echo "=========================================="
    echo "Pre-review 虚假交付检测"
    echo "执行时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "=========================================="
    echo ""

    check_git_repo

    # 确保 main 分支最新
    log_info "同步 main 分支..."
    git fetch origin main 2>/dev/null || log_warn "无法获取远程 main 分支"

    local FAKE_DELIVERY=0
    local current_branch=$(get_current_branch)

    echo "当前分支: $current_branch"
    echo ""

    # 验证 pre-review-hook.sh 存在于 main 分支
    echo "=== 验证 pre-review-hook.sh ==="
    if ! verify_pre_review_hook_in_main; then
        log_error "pre-review-hook.sh 不存在于 origin/main"
        FAKE_DELIVERY=1
        echo ""
        echo "❌ 虚假交付检测失败: pre-review-hook.sh 必须先合并到 main 分支"
        echo ""
        echo "请执行以下步骤:"
        echo "  1. 将 scripts/pre-review-hook.sh 添加到 git"
        echo "  2. 创建 PR 并合并到 main 分支"
        echo "  3. 重新执行此检测"
        FAKE_DELIVERY=$((FAKE_DELIVERY + 1))
    else
        echo "✅ pre-review-hook.sh 存在于 origin/main"
    fi
    echo ""

    # 获取当前 worktree 的根目录
    local WORKTREE_ROOT
    WORKTREE_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)

    echo "=== 验证工作目录修改 ==="

    # 检查是否有未合并到 main 的提交
    local unmerged_commits=$(git log origin/main..HEAD --oneline 2>/dev/null || echo "")

    if [ -n "$unmerged_commits" ]; then
        echo "发现未合并到 main 的提交:"
        echo "$unmerged_commits" | head -10 | sed 's/^/  /'
        echo ""

        # 获取未合并的文件列表
        echo "验证未合并文件的交付状态:"
        local unmerged_files=$(git diff --name-only origin/main..HEAD 2>/dev/null || echo "")

        if [ -n "$unmerged_files" ]; then
            for file in $unmerged_files; do
                # 跳过目录
                if [[ "$file" == */ ]]; then
                    continue
                fi

                # 检查文件是否存在于 main
                if git show origin/main:"$file" > /dev/null 2>&1; then
                    echo "  ✅ $file (已存在于 main)"
                else
                    echo "  ❌ $file (未合并到 main - 疑似虚假交付)"
                    FAKE_DELIVERY=$((FAKE_DELIVERY + 1))
                fi
            done
        fi
    else
        echo "  无未合并到 main 的提交"
    fi
    echo ""

    echo "=========================================="
    echo "检测结果汇总"
    echo "=========================================="

    if [ $FAKE_DELIVERY -gt 0 ]; then
        log_error "检测到疑似虚假交付 ($FAKE_DELIVERY 项)"
        echo ""
        echo "⚠️  在标记 issue 为 in_review 前，请先解决以上问题"
        echo ""
        echo "正确的交付流程:"
        echo "  1. 确保所有交付文件已合并到 main 分支"
        echo "  2. 使用 git show origin/main:<file> 验证文件存在"
        echo "  3. 确保构建和测试通过"
        exit 1
    else
        log_info "未检测到虚假交付"
        echo ""
        echo "✅ 可以安全地将 issue 标记为 in_review"
        exit 0
    fi
}

main "$@"
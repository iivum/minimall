#!/bin/bash
#
# pre-review-hook.sh - Sprint 预审查 Hook
# 在 issue 状态转为 in_review 前验证关键文件是否已合并到 main 分支
# 并检测虚假交付（worktree 中有修改但未合并到 main 分支）
# 用法: ./scripts/pre-review-hook.sh [--files file1 file2 ...] [--skip-fake-detection]
# 退出码: 0 = 验证通过, 1 = 验证失败, 2 = 检测失败

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

VERBOSE=0
FILES=()
SKIP_FAKE_DETECTION=0

usage() {
    echo "用法: $0 [--files file1 file2 ...] [--verbose] [--skip-fake-detection]"
    echo "  --files      指定要验证的关键文件列表（相对于仓库根目录）"
    echo "  --verbose    显示详细输出"
    echo "  --skip-fake-detection  跳过虚假交付检测"
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

log_debug() {
    if [ $VERBOSE -eq 1 ]; then
        echo -e "${BLUE}[DEBUG]${NC} $1"
    fi
}

# 解析参数
while [ $# -gt 0 ]; do
    case "$1" in
        --files)
            shift
            while [ $# -gt 0 ] && [[ ! "$1" == --* ]]; do
                FILES+=("$1")
                shift
            done
            ;;
        --verbose)
            VERBOSE=1
            shift
            ;;
        --skip-fake-detection)
            SKIP_FAKE_DETECTION=1
            shift
            ;;
        *)
            usage
            ;;
    esac
done

# 如果没有指定文件，使用默认的关键文件列表
if [ ${#FILES[@]} -eq 0 ]; then
    FILES=("scripts/pre-review-hook.sh")
fi

# 检查是否为 git 仓库
check_git_repo() {
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        log_error "不是 git 仓库目录"
        exit 2
    fi
}

# 验证文件是否存在于 origin/main
verify_file_in_main() {
    local file_path="$1"

    log_debug "验证文件: $file_path"

    if git show "origin/main:$file_path" > /dev/null 2>&1; then
        log_debug "  ✅ 存在于 main 分支"
        return 0
    else
        log_debug "  ❌ 不存在于 main 分支"
        return 1
    fi
}

# 虚假交付检测
detect_fake_delivery() {
    echo ""
    echo "=== 虚假交付自动检测 ==="
    echo ""

    local FAKE_DELIVERY_COUNT=0

    # 获取所有 worktree
    local WORKTREE_LIST=$(git worktree list --porcelain 2>/dev/null | grep "worktree" | sed 's/worktree //' | tr -d '"')

    if [ -z "$WORKTREE_LIST" ]; then
        log_info "未发现任何 worktree，跳过虚假交付检测"
        return 0
    fi

    echo "检测到的 Worktrees:"
    echo ""

    for worktree_path in $WORKTREE_LIST; do
        # 获取 worktree 的分支名
        local branch=$(cd "$worktree_path" && git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
        echo "  📁 $worktree_path"
        echo "     分支: $branch"

        # 跳过 main 分支的 worktree
        if [ "$branch" = "main" ] || [ "$branch" = "origin/main" ]; then
            echo "     状态: ✅ main 分支，跳过"
            echo ""
            continue
        fi

        # 检查是否有未合并到 main 的提交
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
                    if git ls-tree origin/main "$file" > /dev/null 2>&1; then
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

    if [ $FAKE_DELIVERY_COUNT -gt 0 ]; then
        echo ""
        log_error "检测到 $FAKE_DELIVERY_COUNT 个疑似虚假交付"
        return 1
    else
        log_info "未检测到虚假交付"
        return 0
    fi
}

# 主检测逻辑
main() {
    echo "=========================================="
    echo "Sprint 预审查 Hook"
    echo "执行时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "=========================================="
    echo ""

    check_git_repo

    # 确保 main 分支可用
    log_info "同步 main 分支..."
    if ! git fetch origin main 2>/dev/null; then
        log_warn "无法获取远程 main 分支，将使用本地缓存"
    fi

    # 检查 origin/main 是否存在
    if ! git rev-parse "origin/main" > /dev/null 2>&1; then
        log_error "无法解析 origin/main 分支"
        exit 2
    fi

    echo ""
    echo "=== 验证关键文件 ==="
    echo ""

    FAILED=0
    TOTAL=${#FILES[@]}

    for file in "${FILES[@]}"; do
        echo "检查: $file"

        # 使用 git show 验证文件存在（按issue要求）
        if git show "origin/main:$file" > /dev/null 2>&1; then
            echo -e "  ${GREEN}✅${NC} 文件已合并到 main 分支"
        else
            echo -e "  ${RED}❌${NC} 文件不存在于 main 分支"
            FAILED=$((FAILED + 1))
        fi
        echo ""
    done

    # 虚假交付检测
    if [ $SKIP_FAKE_DETECTION -eq 0 ]; then
        if ! detect_fake_delivery; then
            echo ""
            echo "⚠️  虚假交付检测失败，请先合并相关文件到 main 分支"
            exit 1
        fi
    else
        echo ""
        log_info "已跳过虚假交付检测 (--skip-fake-detection)"
    fi

    echo "=========================================="
    echo "验证结果汇总"
    echo "=========================================="
    echo "总计: $TOTAL"
    echo "通过: $((TOTAL - FAILED))"
    echo "失败: $FAILED"
    echo ""

    if [ $FAILED -gt 0 ]; then
        log_error "验证失败: $FAILED 个关键文件未合并到 main 分支"
        echo ""
        echo "⚠️  请先将上述文件合并到 main 分支后再进行 review"
        echo ""
        echo "建议操作:"
        echo "  1. 确保文件已提交到当前分支"
        echo "  2. 创建 PR 并合并到 main 分支"
        echo "  3. 重新执行此检查"
        exit 1
    else
        log_info "所有关键文件验证通过 ✅"
        echo ""
        echo "✅ 可以继续进行 review"
        exit 0
    fi
}

main "$@"
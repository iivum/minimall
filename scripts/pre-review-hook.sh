#!/bin/bash
#
# pre-review-hook.sh - Sprint 预审查 Hook
# 在 issue 状态转为 in_review 前验证关键文件是否已合并到 main 分支
# 用法: ./scripts/pre-review-hook.sh [--files file1 file2 ...]
# 退出码: 0 = 验证通过, 1 = 验证失败, 2 = 检测失败

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

VERBOSE=0
FILES=()

usage() {
    echo "用法: $0 [--files file1 file2 ...] [--verbose]"
    echo "  --files      指定要验证的关键文件列表（相对于仓库根目录）"
    echo "  --verbose    显示详细输出"
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
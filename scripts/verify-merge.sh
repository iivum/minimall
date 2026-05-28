#!/bin/bash
# verify-merge.sh - 验证代码变更是否已合并到 main 分支
# MIN-3993: CI/CD 流程验收检查清单强化
# 用法: ./verify-merge.sh [options]
# 选项:
#   --file <path>      验证特定文件是否存在于 origin/main
#   --commit <hash>    验证特定 commit 是否已合并
#   --strict           严格模式：必须有文件或 commit 参数
#   --verbose          详细输出
# 示例:
#   ./verify-merge.sh                                    # 检查 main 分支状态
#   ./verify-merge.sh --file src/main/java/App.java       # 验证文件已合并
#   ./verify-merge.sh --commit abc1234                    # 验证 commit 已合并
#   ./verify-merge.sh --strict --file src/main/java/App.java  # 严格验证

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 默认设置
VERBOSE=false
STRICT=false
FILE_PATH=""
COMMIT_HASH=""
CHECK_ONLY=false

usage() {
    echo "用法: $0 [options]"
    echo ""
    echo "选项:"
    echo "  --file <path>      验证特定文件是否存在于 origin/main"
    echo "  --commit <hash>    验证特定 commit 是否已合并"
    echo "  --strict           严格模式：必须提供文件或 commit 参数"
    echo "  --verbose          详细输出"
    echo "  --check            只检查 main 分支状态，不做详细验证"
    echo ""
    echo "示例:"
    echo "  $0 --file src/main/java/App.java"
    echo "  $0 --commit abc1234"
    echo "  $0 --strict --file src/main/java/App.java"
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
    if [ "$VERBOSE" = true ]; then
        echo -e "${BLUE}[DEBUG]${NC} $1"
    fi
}

# 解析参数
while [[ $# -gt 0 ]]; do
    case $1 in
        --file)
            FILE_PATH="$2"
            shift 2
            ;;
        --commit)
            COMMIT_HASH="$2"
            shift 2
            ;;
        --strict)
            STRICT=true
            shift
            ;;
        --verbose)
            VERBOSE=true
            shift
            ;;
        --check)
            CHECK_ONLY=true
            shift
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo "未知选项: $1"
            usage
            ;;
    esac
done

cd "$(dirname "$0")/.."

# 检查 git 环境
log_debug "检查 git 环境..."

if ! git remote -v | grep -q origin; then
    log_error "未找到 origin remote"
    exit 1
fi

# 获取 origin/main 最新提交
log_debug "获取 origin/main 分支信息..."
LOCAL_HASH=$(git rev-parse origin/main 2>/dev/null)
REMOTE_HASH=$(git ls-remote origin main 2>/dev/null | cut -f1)

if [ -z "$LOCAL_HASH" ] || [ -z "$REMOTE_HASH" ]; then
    log_error "无法获取 origin/main 分支信息"
    exit 1
fi

log_debug "本地 origin/main: $LOCAL_HASH"
log_debug "远程 origin/main: $REMOTE_HASH"

# 检查 main 分支是否为最新状态
log_info "检查 main 分支状态..."
if [ "$LOCAL_HASH" != "$REMOTE_HASH" ]; then
    log_warn "origin/main 不是最新状态"
    log_info "正在拉取最新代码..."
    git fetch origin main
    LOCAL_HASH=$(git rev-parse origin/main)
else
    log_info "origin/main 已是最新状态"
fi

echo ""
echo "=========================================="
echo "代码合并验证 (MIN-3993)"
echo "=========================================="
echo ""

RESULT=0

# 如果是 check-only 模式，只验证 main 分支状态
if [ "$CHECK_ONLY" = true ]; then
    log_info "Check-only 模式：只验证 main 分支状态"
    echo ""
    log_info "✓ main 分支状态正常"
    exit 0
fi

# 严格模式检查
if [ "$STRICT" = true ]; then
    if [ -z "$FILE_PATH" ] && [ -z "$COMMIT_HASH" ]; then
        log_error "严格模式：必须提供 --file 或 --commit 参数"
        echo ""
        echo "在严格模式下，必须明确指定要验证的文件或 commit。"
        echo "用法: $0 --strict --file <path> 或 $0 --strict --commit <hash>"
        exit 1
    fi
fi

# 验证文件存在性
if [ -n "$FILE_PATH" ]; then
    log_info "验证文件存在性: $FILE_PATH"

    if git ls-tree origin/main "$FILE_PATH" > /dev/null 2>&1; then
        log_info "✓ 文件已合并到 origin/main: $FILE_PATH"
    else
        log_error "✗ 文件未合并到 origin/main: $FILE_PATH"
        RESULT=1
    fi
    echo ""
fi

# 验证 commit 存在性
if [ -n "$COMMIT_HASH" ]; then
    log_info "验证 commit 存在性: $COMMIT_HASH"

    if git log --oneline origin/main | grep -q "$COMMIT_HASH"; then
        log_info "✓ commit 已合并到 origin/main: $COMMIT_HASH"
    else
        log_error "✗ commit 未合并到 origin/main: $COMMIT_HASH"
        RESULT=1
    fi
    echo ""
fi

# 显示最近的合并记录
log_info "最近 10 个合并到 origin/main 的 commit:"
echo ""
git log --oneline origin/main -10 | sed 's/^/  /'

echo ""

# 如果既没有文件也没有 commit 参数，显示警告
if [ -z "$FILE_PATH" ] && [ -z "$COMMIT_HASH" ]; then
    log_warn "未指定文件或 commit，仅检查 main 分支状态"
    echo ""
    echo "用法提示："
    echo "  $0 --file <path>   验证特定文件是否已合并"
    echo "  $0 --commit <hash> 验证特定 commit 是否已合并"
    echo "  $0 --strict        严格模式（必须提供参数）"
    echo ""
fi

echo ""
if [ $RESULT -eq 0 ]; then
    log_info "✓ 验证通过 - 代码已合并到 main"
else
    log_error "✗ 验证失败 - 部分代码未合并到 main"
fi

exit $RESULT
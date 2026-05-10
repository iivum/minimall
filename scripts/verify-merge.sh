#!/bin/bash
# verify-merge.sh - 验证 issue 声明的文件和代码变更是否确实存在于 main 分支
# 用法: ./verify-merge.sh <issue-id> [file-path] [commit-hash]
# 示例: ./verify-merge.sh MIN-123 src/main/java/Example.java abc1234

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

usage() {
    echo "用法: $0 <issue-id> [file-path] [commit-hash>"
    echo "示例: $0 MIN-123 src/main/java/Example.java abc1234"
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

# 检查参数
if [ -z "$1" ]; then
    usage
fi

ISSUE_ID="$1"
FILE_PATH="$2"
COMMIT_HASH="$3"

cd "$(dirname "$0")/.."

# 检查 main 分支是否为最新状态
log_info "检查 main 分支状态..."

if ! git remote -v | grep -q origin; then
    log_error "未找到 origin remote"
    exit 1
fi

# 获取 origin/main 最新提交
LOCAL_HASH=$(git rev-parse origin/main 2>/dev/null)
REMOTE_HASH=$(git ls-remote origin main 2>/dev/null | cut -f1)

if [ -z "$LOCAL_HASH" ] || [ -z "$REMOTE_HASH" ]; then
    log_error "无法获取 origin/main 分支信息"
    exit 1
fi

log_info "本地 origin/main: $LOCAL_HASH"
log_info "远程 origin/main: $REMOTE_HASH"

if [ "$LOCAL_HASH" != "$REMOTE_HASH" ]; then
    log_warn "origin/main 不是最新状态"
    log_info "正在拉取最新代码..."
    git fetch origin main
else
    log_info "origin/main 已是最新状态"
fi

echo ""
echo "=========================================="
echo "验证 Issue: $ISSUE_ID"
echo "=========================================="
echo ""

RESULT=0

# 验证文件存在性
if [ -n "$FILE_PATH" ]; then
    log_info "验证文件存在性: $FILE_PATH"

    if git ls-tree origin/main "$FILE_PATH" > /dev/null 2>&1; then
        log_info "文件存在于 origin/main"
    else
        log_error "文件不存在于 origin/main: $FILE_PATH"
        RESULT=1
    fi
fi

echo ""

# 验证 commit 存在性
if [ -n "$COMMIT_HASH" ]; then
    log_info "验证 commit 存在性: $COMMIT_HASH"

    if git log --oneline origin/main | grep -q "$COMMIT_HASH"; then
        log_info "commit 存在于 origin/main"
    else
        log_error "commit 不存在于 origin/main: $COMMIT_HASH"
        RESULT=1
    fi
fi

echo ""

# 显示最近的合并记录
log_info "最近 10 个合并到 origin/main 的 commit:"
echo ""
git log --oneline origin/main -10 | sed 's/^/  /'

echo ""
if [ $RESULT -eq 0 ]; then
    log_info "验证通过!"
else
    log_error "验证失败!"
fi

exit $RESULT
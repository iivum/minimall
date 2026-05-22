#!/bin/bash
# verify-commit-hash.sh - 验证 commit hash 是否存在于 origin/main
# 用法: ./verify-commit-hash.sh <commit-hash>
# 返回码: 0=存在, 1=不存在

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

usage() {
    echo "用法: $0 <commit-hash>"
    echo "示例: $0 abc1234"
    exit 1
}

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查参数
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

# 验证 commit 是否存在于 origin/main
log_info "验证 commit: $COMMIT_HASH"

if git log --oneline origin/main | grep -q "$COMMIT_HASH"; then
    log_info "commit 存在于 origin/main"
    exit 0
else
    log_error "commit 不存在于 origin/main: $COMMIT_HASH"
    exit 1
fi

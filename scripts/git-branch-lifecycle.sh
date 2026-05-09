#!/bin/bash
# 识别可以清理的已合并 Agent 分支

set -euo pipefail

TARGET_BRANCH="${1:-main}"
PROTECTED_PATTERNS="main|master|release|feature|hotfix"

echo "=== 已合并到 ${TARGET_BRANCH} 的 Agent 分支 ==="
echo ""

git fetch origin "${TARGET_BRANCH}" --quiet 2>/dev/null || true

for branch in $(git branch -r --merged "origin/${TARGET_BRANCH}" 2>/dev/null | grep -E '^  origin/agent/' | sed 's/^  origin\///'); do
    if echo "$branch" | grep -qE "^origin/(${PROTECTED_PATTERNS})"; then
        continue
    fi
    last_commit=$(git log -1 --format="%ai" "$branch" --since="30 days ago" 2>/dev/null || echo "old")
    echo "- $branch (最后提交: $last_commit)"
    echo "  删除命令: git push origin --delete ${branch#origin/}"
done

echo ""
echo "=== 未合并的 Agent 分支 ==="
echo ""

for branch in $(git branch -r --no-merged "origin/${TARGET_BRANCH}" 2>/dev/null | grep -E '^  origin/agent/' | sed 's/^  origin\///'); do
    echo "- $branch"
done
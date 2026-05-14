#!/bin/bash
#
# verify-deliverables.sh - 验证交付物文件是否存在
# 用法: ./scripts/verify-deliverables.sh file1 file2 ...
# 退出码: 0 = 全部存在, 1 = 任意文件缺失

set -e

usage() {
    echo "用法: $0 <文件1> [文件2] ..."
    echo "验证每个文件是否存在，使用 test -f 检查"
    exit 1
}

if [ $# -eq 0 ]; then
    usage
fi

FAILED=0
TOTAL=0

echo "=== 交付物验证 ==="
echo ""

for file in "$@"; do
    TOTAL=$((TOTAL + 1))
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file (不存在)"
        FAILED=$((FAILED + 1))
    fi
done

echo ""
echo "=== 汇总 ==="
echo "总计: $TOTAL"
echo "通过: $((TOTAL - FAILED))"
echo "失败: $FAILED"

if [ $FAILED -gt 0 ]; then
    echo ""
    echo "❌ 验证失败: $FAILED 个文件缺失"
    exit 1
else
    echo ""
    echo "✅ 全部验证通过"
    exit 0
fi
#!/bin/bash
#
# verify-deliverables.sh - 验证交付物文件或目录是否存在
# 用法: ./scripts/verify-deliverables.sh <文件或目录1> [文件或目录2] ...
# 退出码: 0 = 全部存在, 1 = 任意路径缺失

set -e

usage() {
    echo "用法: $0 <文件或目录1> [文件或目录2] ..."
    echo "验证每个路径是否存在，文件用 test -f，目录用 test -d"
    exit 1
}

if [ $# -eq 0 ]; then
    usage
fi

FAILED=0
TOTAL=0

echo "=== 交付物验证 ==="
echo ""

for path in "$@"; do
    TOTAL=$((TOTAL + 1))
    if [ -f "$path" ]; then
        echo "✅ $path (文件存在)"
    elif [ -d "$path" ]; then
        echo "✅ $path (目录存在)"
    else
        echo "❌ $path (不存在)"
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
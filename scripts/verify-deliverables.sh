#!/bin/bash
# 验证交付物文件存在性
# 用法: ./scripts/verify-deliverables.sh file1 file2 ...

set -e

if [ $# -eq 0 ]; then
    echo "用法: $0 <file1> [file2] ..."
    echo "验证所有指定文件是否存在"
    exit 1
fi

FAILED=0
for file in "$@"; do
    if [ -f "$file" ]; then
        echo "✓ $file - 存在"
    else
        echo "✗ $file - 不存在"
        FAILED=1
    fi
done

if [ $FAILED -eq 1 ]; then
    echo ""
    echo "验证失败: 某些文件不存在"
    exit 1
else
    echo ""
    echo "验证成功: 所有文件存在"
    exit 0
fi
#!/bin/bash
# verify-deliverables.sh - 最终交付物验证脚本
# 用法: ./scripts/verify-deliverables.sh "file1" "file2" "file3" ...

set -e

echo "=== 团队驱动交付物最终校验 ==="
echo ""

if [ $# -eq 0 ]; then
  echo "用法: $0 <file1> [file2] [file3] ..."
  echo "示例: $0 src/main/java/Example.java src/test/java/ExampleTest.java"
  exit 1
fi

MISSING_COUNT=0

for file in "$@"; do
  if test -f "$file"; then
    echo "✓ $file"
  else
    echo "✗ MISSING: $file"
    MISSING_COUNT=$((MISSING_COUNT + 1))
  fi
done

echo ""
if [ $MISSING_COUNT -eq 0 ]; then
  echo "=== 校验通过：所有交付物存在 ==="
  exit 0
else
  echo "=== 校验失败：$MISSING_COUNT 个文件缺失 ==="
  exit 1
fi

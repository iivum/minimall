#!/bin/bash
#
# check-modifying-annotation.sh - 检查 @Modifying 注解是否设置了 clearAutomatically = true
#
# 此脚本检测所有使用 @Query 注解进行 DELETE 或 UPDATE 操作的 JPA Repository 方法，
# 确保它们都正确配置了 @Modifying(clearAutomatically = true)
#
# 退出码: 0 = 所有检查通过, 1 = 存在违规

set -e

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

echo "=== @Modifying clearAutomatically 检查 ==="
echo ""

# 临时文件存储检测结果
VIOLATIONS=$(mktemp)
ERRORS=$(mktemp)
trap "rm -f $VIOLATIONS $ERRORS" EXIT

# 查找所有使用 @Query 的 Repository 文件
REPO_FILES=$(find src/main/java -name "*Repository.java" -type f)

if [ -z "$REPO_FILES" ]; then
    echo "未找到任何 Repository 文件，跳过检查"
    exit 0
fi

echo "检查以下 Repository 文件:"
echo "$REPO_FILES" | while read -r file; do
    echo "  - $file"
done
echo ""

# 对每个 Repository 文件进行检查
while IFS= read -r repo_file; do
    # 检查是否存在 @Query DELETE 或 UPDATE 操作
    if ! grep -q '@Query.*"DELETE\|@Query.*"UPDATE' "$repo_file"; then
        continue
    fi

    echo "检查文件: $repo_file"

    # 用 Perl 进行更精确的检测（跨平台兼容）
    # 检测 @Query DELETE/UPDATE 但缺少 @Modifying(clearAutomatically = true) 的方法
    perl -n -e '
        my $method_name = "";
        my $has_modifying = 0;
        my $clear_auto = 0;
        my $in_query_delete = 0;

        # 匹配方法声明
        if (/^\s*(?:void|\w+)\s+(\w+)\s*\([^)]*\)\s*\{/) {
            $method_name = $1;
        }

        # 匹配 @Modifying 注解
        if (/\@Modifying/) {
            $has_modifying = 1;
            $clear_auto = /clearAutomatically\s*=\s*true/ ? 1 : 0;
        }

        # 匹配 @Query DELETE 或 UPDATE
        if (/\@Query.*"DELETE|@Query.*"UPDATE/) {
            $in_query_delete = 1;
        }

        # 方法结束
        if (/^\s*\}/ && $in_query_delete) {
            if (!$has_modifying || !$clear_auto) {
                print "  [VIOLATION] 方法 $method_name - " .
                      (!$has_modifying ? "缺少 @Modifying 注解" : "@Modifying clearAutomatically 未设置为 true") . "\n";
            }
            $in_query_delete = 0;
            $has_modifying = 0;
            $clear_auto = 0;
        }
    ' "$repo_file" >> "$VIOLATIONS" 2>> "$ERRORS"

done <<< "$REPO_FILES"

# 显示任何检测错误（不影响主流程）
if [ -s "$ERRORS" ] && [ ! -s "$VIOLATIONS" ]; then
    cat "$ERRORS" >&2
fi

# 检查是否有违规
if [ -s "$VIOLATIONS" ]; then
    echo ""
    echo "============================================"
    echo "❌ 检查失败: 发现以下违规:"
    echo "============================================"
    cat "$VIOLATIONS"
    echo ""
    echo "============================================"
    echo ""
    echo "@Modifying(clearAutomatically = true) 是必需的，因为:"
    echo "  1. 防止 stale data 问题（EntityManager 中的旧数据）"
    echo "  2. 确保后续查询能看到更新后的数据"
    echo "  3. 避免潜在的 data inconsistency"
    echo ""
    echo "请修复上述问题后重新提交"
    exit 1
fi

echo ""
echo "✅ 所有 @Modifying 注解检查通过"
echo "所有使用 @Query DELETE/UPDATE 的方法都正确配置了 @Modifying(clearAutomatically = true)"
exit 0
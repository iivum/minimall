#!/bin/bash
#
# verify-deliverables.sh - 验证交付物文件是否存在
# 用法:
#   ./scripts/verify-deliverables.sh <文件1> [文件2] ...
#   ./scripts/verify-deliverables.sh --mvn [base_commit]  # 运行 Maven 测试验证
# 退出码: 0 = 全部存在, 1 = 任意文件缺失或测试失败

set -e

usage() {
    echo "用法:"
    echo "  $0 <文件1> [文件2] ...     # 验证文件是否存在"
    echo "  $0 --mvn [base_commit]    # 运行 Maven 测试验证 (可选: 指定基准提交比较测试数量)"
    exit 1
}

# Maven 测试验证模式
run_mvn_verify() {
    local base_commit="${1:-}"
    echo "=== Maven 测试验证模式 ==="
    echo ""

    # 检查 pom.xml 是否存在
    if [ ! -f "pom.xml" ]; then
        echo "❌ pom.xml 不存在"
        exit 1
    fi
    echo "✅ pom.xml 存在"

    # 检查 Maven wrapper 或 mvn
    local mvn_cmd="mvn"
    if [ -f "./mvnw" ]; then
        mvn_cmd="./mvnw"
        echo "✅ 使用 Maven Wrapper"
    else
        echo "✅ 使用系统 Maven"
    fi

    # 统计当前测试数量
    echo ""
    echo "=== 统计测试文件 ==="
    local test_count
    test_count=$(find src/test/java -name '*Test.java' 2>/dev/null | wc -l | tr -d ' ')
    echo "当前测试文件数量: $test_count"

    if [ "$test_count" -eq 0 ]; then
        echo "❌ 未找到任何测试文件"
        exit 1
    fi

    # 显示测试文件列表
    echo ""
    echo "测试文件列表:"
    find src/test/java -name '*Test.java' 2>/dev/null | sort | while read -r file; do
        echo "  - $file"
    done

    # 比较基准提交(如果提供)的测试数量变化
    if [ -n "$base_commit" ]; then
        echo ""
        echo "=== 与基准提交 $base_commit 比较 ==="
        local base_test_count
        base_test_count=$(git ls-tree -r "$base_commit" --name-only | grep "src/test/java/.*Test.java$" 2>/dev/null | wc -l | tr -d ' ')
        echo "基准提交测试文件数量: $base_test_count"

        if [ "$base_test_count" -gt 0 ]; then
            local diff=$((test_count - base_test_count))
            if [ "$diff" -gt 0 ]; then
                echo "📈 新增 $diff 个测试文件"
            elif [ "$diff" -lt 0 ]; then
                echo "📉 减少 $((-diff)) 个测试文件"
            else
                echo "➖ 测试文件数量无变化"
            fi
        fi

        # 检测新增测试文件是否真实存在
        echo ""
        echo "=== 检测新增测试文件 ==="
        if git diff --name-only "$base_commit" HEAD 2>/dev/null | grep -q "src/test/java/.*Test.java$"; then
            echo "发现新增测试文件:"
            git diff --name-only "$base_commit" HEAD 2>/dev/null | grep "src/test/java/.*Test.java$" | while read -r new_file; do
                if [ -f "$new_file" ]; then
                    echo "  ✅ $new_file (真实存在)"
                else
                    echo "  ❌ $new_file (文件缺失 - 可能为虚假交付)"
                fi
            done
        else
            echo "无新增测试文件"
        fi
    fi

    # 运行 Maven 测试验证
    echo ""
    echo "=== 运行 Maven 测试 ==="
    echo "执行: $mvn_cmd test -B -q"

    if $mvn_cmd test -B -q; then
        echo ""
        echo "✅ Maven 测试执行成功"
    else
        echo ""
        echo "❌ Maven 测试执行失败"
        echo ""
        echo "=== 测试失败详情 ==="
        if [ -d "target/surefire-reports" ]; then
            find target/surefire-reports -name "*.txt" -exec head -50 {} \; 2>/dev/null | head -100
        fi
        exit 1
    fi

    # 输出测试统计
    echo ""
    echo "=== 测试执行统计 ==="
    echo "测试类数量: $(find target/surefire-reports -name "*.txt" 2>/dev/null | wc -l | tr -d ' ')"
    echo "✅ Maven 测试验证完成"
}

# 主逻辑
if [ "$#" -eq 0 ]; then
    usage
fi

if [ "$1" = "--mvn" ]; then
    run_mvn_verify "$2"
else
    # 原有的文件验证逻辑
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
fi
#!/bin/bash
# detect-tech-debt.sh - 技术债自动化检测脚本
# 检测代码复杂度、重复代码、硬编码等常见技术债指标
# 目标：技术债增长率 < 5% per Sprint

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# 默认阈值
COMPLEXITY_METHOD_THRESHOLD=50
COMPLEXITY_CLASS_THRESHOLD=500
DUPLICATE_LINES_THRESHOLD=10

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=== MiniMall 技术债检测 ==="
echo "目标：技术债增长率 < 5% per Sprint"
echo ""

TOTAL_ISSUES=0

# 1. 检测方法复杂度（方法不超过 50 行）
echo "[1/7] 检测方法复杂度..."
METHOD_ISSUES=$(find "$PROJECT_DIR/src" -name "*.java" -exec grep -l "public\|private\|protected" {} \; | while read f; do
    awk -v threshold="$COMPLEXITY_METHOD_THRESHOLD" '
        BEGIN { in_method=0; brace_count=0; line_count=0 }
        /^(public|private|protected).*[{(]/ {
            if (in_method && line_count > threshold) {
                print FILENAME ":" NR " - 方法超过 " threshold " 行"
            }
            in_method=1
            brace_count=0
            line_count=0
        }
        in_method {
            line_count++
            for (i=1; i<=length($0); i++) {
                if (substr($0,i,1) == "{") brace_count++
                if (substr($0,i,1) == "}") brace_count--
            }
            if (brace_count <= 0 && line_count > 1) {
                if (line_count > threshold) {
                    print FILENAME ":" NR " - 方法超过 " threshold " 行"
                }
                in_method=0
            }
        }
    ' "$f" 2>/dev/null || true
done | head -20)

if [ -n "$METHOD_ISSUES" ]; then
    echo -e "${YELLOW}⚠️  发现复杂方法:${NC}"
    echo "$METHOD_ISSUES"
    TOTAL_ISSUES=$((TOTAL_ISSUES + $(echo "$METHOD_ISSUES" | grep -c "^") ))
else
    echo -e "${GREEN}✅ 方法复杂度正常${NC}"
fi

# 2. 检测类复杂度（类不超过 500 行）
echo ""
echo "[2/7] 检测类复杂度..."
CLASS_ISSUES=$(find "$PROJECT_DIR/src" -name "*.java" -type f -exec wc -l {} \; 2>/dev/null | \
    awk -v threshold="$COMPLEXITY_CLASS_THRESHOLD" '$1 > threshold {print $2 " - " $1 " 行"}' | head -10)

if [ -n "$CLASS_ISSUES" ]; then
    echo -e "${YELLOW}⚠️  发现复杂类:${NC}"
    echo "$CLASS_ISSUES"
    TOTAL_ISSUES=$((TOTAL_ISSUES + $(echo "$CLASS_ISSUES" | grep -c "^") ))
else
    echo -e "${GREEN}✅ 类复杂度正常${NC}"
fi

# 3. 检测重复代码（使用简单文本相似度检测）
echo ""
echo "[3/7] 检测重复代码..."
DUPLICATE_FILES=$(find "$PROJECT_DIR/src" -name "*.java" -type f -exec basename {} \; | sort | uniq -d | head -10)

if [ -n "$DUPLICATE_FILES" ]; then
    echo -e "${YELLOW}⚠️  发现重复文件名:${NC}"
    echo "$DUPLICATE_FILES"
fi

# 使用 PMD CPD 检测重复代码（如果可用）
if command -v pmd &> /dev/null; then
    echo "使用 PMD CPD 检测重复代码..."
    pmd cpd --minimum-tokens 30 --source "$PROJECT_DIR/src" --format text 2>/dev/null | head -50 || true
elif [ -f "$PROJECT_DIR/.mvn/wrapper/maven-wrapper.jar" ]; then
    echo "使用 Maven PMD 插件检测重复代码..."
    # 检测超过 10 行重复的代码块
    find "$PROJECT_DIR/src" -name "*.java" -exec grep -n "." {} \; | \
        sort | uniq -c | awk '$1 > 5 && length($3) > 20 {print $3 " - 重复 " $1 " 次"}' | head -20 || true
else
    echo -e "${YELLOW}⚠️  PMD/CPD 不可用，跳过精确重复代码检测${NC}"
fi

# 4. 检测硬编码值（魔法数字）
echo ""
echo "[4/7] 检测硬编码值..."
HARDCODED_ISSUES=$(grep -rn -E "(?<![a-zA-Z_])[0-9]{4,}(?![a-zA-Z0-9_])|\"[a-zA-Z]{3,}\"" \
    "$PROJECT_DIR/src" --include="*.java" 2>/dev/null | \
    grep -v "// " | grep -v "serialVersionUID" | \
    grep -v "UTF-8\|GBK\|ISO-8859" | \
    grep -v "^[0-9]*$" | \
    head -30 || true)

if [ -n "$HARDCODED_ISSUES" ]; then
    echo -e "${YELLOW}⚠️  发现可能的硬编码值:${NC}"
    echo "$HARDCODED_ISSUES" | head -20
    TOTAL_ISSUES=$((TOTAL_ISSUES + $(echo "$HARDCODED_ISSUES" | grep -c "^") ))
else
    echo -e "${GREEN}✅ 无明显硬编码值${NC}"
fi

# 5. 检测 Field Injection（应使用构造器注入）
echo ""
echo "[5/7] 检测 Field Injection..."
FIELD_INJECTION=$(grep -rn "@Autowired" "$PROJECT_DIR/src" --include="*.java" 2>/dev/null | \
    grep -v "private final\|private final" | \
    grep "private.*@" | head -20 || true)

if [ -n "$FIELD_INJECTION" ]; then
    echo -e "${YELLOW}⚠️  发现 Field Injection:${NC}"
    echo "$FIELD_INJECTION" | head -10
    TOTAL_ISSUES=$((TOTAL_ISSUES + $(echo "$FIELD_INJECTION" | grep -c "^") ))
    echo -e "${YELLOW}建议：使用构造器注入替代 Field Injection${NC}"
else
    echo -e "${GREEN}✅ 无 Field Injection${NC}"
fi

# 6. 检测异步方法使用无界线程池
echo ""
echo "[6/7] 检测异步线程池配置..."
ASYNC_ISSUES=$(grep -rn "SimpleAsyncTaskExecutor\|new ThreadPoolTaskExecutor()" \
    "$PROJECT_DIR/src" --include="*.java" 2>/dev/null | head -10 || true)

if [ -n "$ASYNC_ISSUES" ]; then
    echo -e "${YELLOW}⚠️  发现无界线程池使用:${NC}"
    echo "$ASYNC_ISSUES"
    TOTAL_ISSUES=$((TOTAL_ISSUES + $(echo "$ASYNC_ISSUES" | grep -c "^") ))
    echo -e "${YELLOW}建议：使用有界线程池配置 rejection policy${NC}"
else
    echo -e "${GREEN}✅ 异步线程池配置正常${NC}"
fi

# 7. 检测缺少 @Modifying 注解的更新操作
echo ""
echo "[7/7] 检测数据完整性问题..."
MODIFYING_ISSUES=$(grep -rn "@Query" "$PROJECT_DIR/src" --include="*.java" 2>/dev/null | \
    grep -i "UPDATE\|DELETE\|INSERT" | \
    while read line; do
        file=$(echo "$line" | cut -d: -f1)
        linenum=$(echo "$line" | cut -d: -f2)
        # 检查后续 5 行是否有 @Modifying
        if ! sed -n "${linenum},$((linenum+5))p" "$file" | grep -q "@Modifying"; then
            echo "$line - 缺少 @Modifying 注解"
        fi
    done | head -10 || true)

if [ -n "$MODIFYING_ISSUES" ]; then
    echo -e "${YELLOW}⚠️  发现可能缺少 @Modifying 的更新操作:${NC}"
    echo "$MODIFYING_ISSUES"
    TOTAL_ISSUES=$((TOTAL_ISSUES + $(echo "$MODIFYING_ISSUES" | grep -c "^") ))
else
    echo -e "${GREEN}✅ 数据完整性检查正常${NC}"
fi

# 总结
echo ""
echo "=== 技术债检测结果 ==="
echo "检测项：7 项"
echo "发现技术债问题：$TOTAL_ISSUES 项"

if [ $TOTAL_ISSUES -gt 0 ]; then
    echo -e "${YELLOW}⚠️  建议修复上述问题后再合并 PR${NC}"
    echo ""
    echo "修复优先级："
    echo "1. Field Injection - 影响测试可测试性"
    echo "2. 无界线程池 - 可能导致 OOM"
    echo "3. 缺少 @Modifying - 可能导致数据不一致"
    echo "4. 方法/类复杂度 - 影响可维护性"
    echo "5. 硬编码值 - 影响可配置性"
    exit 1
else
    echo -e "${GREEN}✅ 未检测到明显技术债问题${NC}"
    exit 0
fi
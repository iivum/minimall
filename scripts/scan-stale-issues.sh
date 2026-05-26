#!/bin/bash
#
# scan-stale-issues.sh - Scan for stale issues in backlog
#
# Scans backlog issues that haven't been processed in more than 50 sprints
# (approximately 50 weeks / ~1 year) and outputs a report.
#
# Usage: ./scripts/scan-stale-issues.sh [--json]
#

set -euo pipefail

# Configuration
SPRINT_THRESHOLD=50
DAYS_THRESHOLD=$((SPRINT_THRESHOLD * 7))  # ~350 days
OUTPUT_JSON=false

# Parse arguments
if [[ "${1:-}" == "--json" ]]; then
    OUTPUT_JSON=true
fi

# Colors (disabled if not a terminal)
if [[ -t 1 ]]; then
    RED='\033[0;31m'
    YELLOW='\033[1;33m'
    GREEN='\033[0;32m'
    NC='\033[0m'
else
    RED=''
    YELLOW=''
    GREEN=''
    NC=''
fi

# Get current date in seconds since epoch
CURRENT_DATE=$(date +%s)

# Calculate cutoff date (50 sprints ago)
CUTOFF_DATE=$((CURRENT_DATE - DAYS_THRESHOLD * 86400))

echo "=== Stale Issue Scanner ==="
echo "Scanning for issues in backlog older than $SPRINT_THRESHOLD sprints (~${DAYS_THRESHOLD} days)"
echo "Cutoff date: $(date -r "$CUTOFF_DATE" '+%Y-%m-%d')"
echo ""

# Fetch all backlog issues with pagination
declare -a STALE_ISSUES=()
declare -a STALE_IDENTIFIERS=()
TOTAL_COUNT=0
STALE_COUNT=0

echo "Fetching backlog issues..."

# Use offset-based pagination to get all issues
OFFSET=0
LIMIT=50
HAS_MORE=true

while [[ "$HAS_MORE" == "true" ]]; do
    # Fetch batch of issues
    BATCH=$(multica issue list --status backlog --limit "$LIMIT" --offset "$OFFSET" --output json 2>/dev/null || echo '{"issues": []}')

    # Check if there are more issues
    if ! echo "$BATCH" | jq -e '.has_more' >/dev/null 2>&1; then
        HAS_MORE=false
        break
    fi

    BATCH_COUNT=$(echo "$BATCH" | jq '.issues | length')
    if [[ "$BATCH_COUNT" -eq 0 ]]; then
        break
    fi

    # Process each issue in batch
    IDS=$(echo "$BATCH" | jq -r '.issues[].id')
    for ISSUE_ID in $IDS; do
        ((TOTAL_COUNT++)) || true

        # Get issue creation date
        CREATED_AT=$(echo "$BATCH" | jq -r --arg id "$ISSUE_ID" '.issues[] | select(.id == $id) | .created_at')
        CREATED_DATE=$(date -d "$CREATED_AT" +%s 2>/dev/null || date -j -f "%Y-%m-%dT%H:%M:%SZ" "$CREATED_AT" +%s 2>/dev/null)

        # Check if issue is older than threshold
        if [[ "$CREATED_DATE" -lt "$CUTOFF_DATE" ]]; then
            ((STALE_COUNT++)) || true

            # Extract issue details
            ISSUE_IDENTIFIER=$(echo "$BATCH" | jq -r --arg id "$ISSUE_ID" '.issues[] | select(.id == $id) | .identifier')
            ISSUE_TITLE=$(echo "$BATCH" | jq -r --arg id "$ISSUE_ID" '.issues[] | select(.id == $id) | .title')
            ISSUE_PRIORITY=$(echo "$BATCH" | jq -r --arg id "$ISSUE_ID" '.issues[] | select(.id == $id) | .priority // "unknown"')
            ISSUE_AGE_DAYS=$(( (CURRENT_DATE - CREATED_DATE) / 86400 ))
            ISSUE_SPRINTS=$(( ISSUE_AGE_DAYS / 7 ))

            STALE_ISSUES+=("$ISSUE_ID|$ISSUE_IDENTIFIER|$ISSUE_TITLE|$ISSUE_PRIORITY|$ISSUE_AGE_DAYS|$ISSUE_SPRINTS")
            STALE_IDENTIFIERS+=("$ISSUE_IDENTIFIER")
        fi
    done

    # Check if there are more issues to fetch
    HAS_MORE=$(echo "$BATCH" | jq -r '.has_more // false')

    if [[ "$HAS_MORE" == "true" ]]; then
        OFFSET=$((OFFSET + LIMIT))
    fi
done

echo "Total backlog issues scanned: $TOTAL_COUNT"
echo "Stale issues found (> $SPRINT_THRESHOLD sprints): $STALE_COUNT"
echo ""

if [[ "$STALE_COUNT" -eq 0 ]]; then
    echo -e "${GREEN}No stale issues found. All backlog issues are recent.${NC}"
    exit 0
fi

# Output report
echo "=== Stale Issues Report ==="
printf "%-12s %-10s %-8s %s\n" "IDENTIFIER" "PRIORITY" "SPRINTS" "TITLE"
printf "%-12s %-10s %-8s %s\n" "----------" "--------" "-------" "-----"

for ISSUE_DATA in "${STALE_ISSUES[@]}"; do
    IFS='|' read -r ISSUE_ID ISSUE_IDENTIFIER ISSUE_TITLE ISSUE_PRIORITY ISSUE_AGE_DAYS ISSUE_SPRINTS <<< "$ISSUE_DATA"

    # Color code by priority
    case "$ISSUE_PRIORITY" in
        critical)
            COLOR="$RED"
            ;;
        high)
            COLOR="$YELLOW"
            ;;
        *)
            COLOR="$NC"
            ;;
    esac

    printf "${COLOR}%-12s %-10s %-8s %s${NC}\n" "$ISSUE_IDENTIFIER" "$ISSUE_PRIORITY" "$ISSUE_SPRINTS" "$ISSUE_TITLE"
done

echo ""
echo "=== Summary Statistics ==="
echo "Total backlog issues: $TOTAL_COUNT"
echo "Stale issues: $STALE_COUNT"
echo "Fresh issues: $((TOTAL_COUNT - STALE_COUNT))"
echo "Stale percentage: $(awk "BEGIN {printf \"%.1f\", ($STALE_COUNT / $TOTAL_COUNT) * 100}")%"

# Output JSON if requested
if [[ "$OUTPUT_JSON" == "true" ]]; then
    echo ""
    echo "=== JSON Output ==="

    JSON_OUTPUT=$(jq -n \
        --argjson stale_count "$STALE_COUNT" \
        --argjson total_count "$TOTAL_COUNT" \
        --argjson cutoff_date "$(date -r "$CUTOFF_DATE" '+%Y-%m-%d')" \
        --argjson threshold_sprints "$SPRINT_THRESHOLD" \
        --argjson scan_date "$(date '+%Y-%m-%d %H:%M:%S')" \
        '{
            scan_date: $scan_date,
            config: {
                threshold_sprints: $threshold_sprints,
                cutoff_date: $cutoff_date
            },
            summary: {
                total_backlog_issues: $total_count,
                stale_issues: $stale_count,
                fresh_issues: ($total_count - $stale_count),
                stale_percentage: (($stale_count / $total_count) * 100 | floor)
            },
            stale_issues: []
        }')

    # Add each stale issue to JSON
    for ISSUE_DATA in "${STALE_ISSUES[@]}"; do
        IFS='|' read -r ISSUE_ID ISSUE_IDENTIFIER ISSUE_TITLE ISSUE_PRIORITY ISSUE_AGE_DAYS ISSUE_SPRINTS <<< "$ISSUE_DATA"

        ISSUE_JSON=$(jq -n \
            --arg id "$ISSUE_ID" \
            --arg identifier "$ISSUE_IDENTIFIER" \
            --arg title "$ISSUE_TITLE" \
            --arg priority "$ISSUE_PRIORITY" \
            --argjson age_days "$ISSUE_AGE_DAYS" \
            --argjson age_sprints "$ISSUE_SPRINTS" \
            '{
                id: $id,
                identifier: $identifier,
                title: $title,
                priority: $priority,
                age_days: $age_days,
                age_sprints: $age_sprints
            }')

        JSON_OUTPUT=$(echo "$JSON_OUTPUT" | jq --argjson issue "$ISSUE_JSON" '.stale_issues += [$issue]')
    done

    echo "$JSON_OUTPUT"
fi

# Exit with appropriate code
if [[ "$STALE_COUNT" -gt 0 ]]; then
    exit 0  # Found stale issues, but not an error
else
    exit 0
fi
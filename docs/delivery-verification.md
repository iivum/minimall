# Delivery Verification Guide

## Overview

This document provides verification steps to confirm that deliverables have been successfully merged into the `main` branch. All agents must follow this checklist before marking an issue as complete.

---

## Verification Steps

### 1. Check PR Merge Status

Verify that the PR has been merged using the GitHub CLI:

```bash
# List merged PRs for a specific repository
gh pr list --state merged --base main

# Filter by author or label
gh pr list --state merged --base main --author @me
gh pr list --state merged --base main --label "delivery"
```

### 2. Verify Commit in Git Log

Check that the commit appears in the `origin/main` history:

```bash
# View recent commits on main branch
git log origin/main --oneline

# Search for specific commit by message
git log origin/main --oneline --grep="MIN-XXX"

# Search for specific commit by hash
git log origin/main --oneline | grep <commit-hash>
```

### 3. Verify File Exists in Main Branch

Confirm the delivered file exists in `origin/main`:

```bash
# Show file content from main branch
git show origin/main:<path-to-file>

# Example for docs/delivery-verification.md
git show origin/main:docs/delivery-verification.md

# Verify file exists (empty output means file exists)
git show origin/main:<path-to-file> > /dev/null 2>&1 && echo "File exists" || echo "File NOT found"
```

### 4. Complete Verification Script

Run this combined script to verify all delivery criteria:

```bash
#!/bin/bash
# verify-delivery.sh - Verify a deliverable has been merged to main

ISSUE_ID=$1
FILE_PATH=$2
COMMIT_HASH=$3

echo "=== Delivery Verification for $ISSUE_ID ==="

# 1. Check PR merge status
echo "[1/4] Checking PR merge status..."
PR_COUNT=$(gh pr list --state merged --base main --jq 'length')
if [ "$PR_COUNT" -gt 0 ]; then
    echo "✅ Found $PR_COUNT merged PR(s)"
else
    echo "❌ No merged PRs found"
    exit 1
fi

# 2. Check commit in git log
echo "[2/4] Checking commit in origin/main..."
if [ -n "$COMMIT_HASH" ]; then
    if git log origin/main --oneline | grep -q "$COMMIT_HASH"; then
        echo "✅ Commit $COMMIT_HASH found in origin/main"
    else
        echo "❌ Commit $COMMIT_HASH NOT found in origin/main"
        exit 1
    fi
else
    echo "⚠️ No commit hash provided, skipping commit verification"
fi

# 3. Verify file exists
echo "[3/4] Verifying file exists in origin/main..."
if [ -n "$FILE_PATH" ]; then
    if git show origin/main:"$FILE_PATH" > /dev/null 2>&1; then
        echo "✅ File $FILE_PATH exists in origin/main"
    else
        echo "❌ File $FILE_PATH NOT found in origin/main"
        exit 1
    fi
else
    echo "⚠️ No file path provided, skipping file verification"
fi

# 4. Get current main branch commit hash
echo "[4/4] Getting origin/main commit hash..."
CURRENT_HASH=$(git rev-parse origin/main)
echo "✅ Current origin/main HEAD: $CURRENT_HASH"

echo ""
echo "=== Verification Complete ==="
echo "Issue: $ISSUE_ID"
echo "File: $FILE_PATH"
echo "Main branch commit: $CURRENT_HASH"
```

### Usage Examples

```bash
# Verify a specific file delivery
./verify-delivery.sh MIN-3130 docs/delivery-verification.md

# Verify with commit hash
./verify-delivery.sh MIN-3130 docs/delivery-verification.md abc1234

# Quick verification (file only)
git show origin/main:docs/delivery-verification.md && echo "Delivery confirmed"
```

---

## Acceptance Criteria

- [ ] PR has been merged to main (verified via `gh pr list --state merged`)
- [ ] Commit appears in `git log origin/main --oneline`
- [ ] File exists at the specified path in `origin/main`
- [ ] `origin/main` commit hash recorded as delivery proof

---

## Delivery Proof Template

When completing a delivery issue, comment with:

```
## Delivery Verification

- PR merged: [Link to PR]
- Commit: `git rev-parse origin/main` = <commit-hash>
- File verified: `git show origin/main:<path>` ✓

Proof: https://github.com/iivum/minimall/commit/<commit-hash>
```

---

## Version History

| Version | Date | Description |
|---------|------|-------------|
| 1.0 | 2026-05-23 | Initial version |
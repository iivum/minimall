# Delivery Verification

## Overview

This document provides verification steps to confirm that PRs have been successfully merged into the `main` branch and that the changes are visible in the remote repository.

## Verification Steps

### 1. Check if PR is Merged

Use the GitHub CLI to verify the PR has been merged:

```bash
gh pr list --state merged --base main
```

For a specific PR:

```bash
gh pr list --state merged --base main --search "your-pr-title"
```

### 2. Verify Commit in git log

Check if the commit appears in the main branch history:

```bash
git log origin/main --oneline | grep "your-commit-message"
```

Or view the recent commits on main:

```bash
git log origin/main --oneline -20
```

### 3. Verify File Exists on main

Confirm the file exists in the remote main branch:

```bash
git show origin/main:docs/delivery-verification.md
```

Or using GitHub CLI:

```bash
gh api repos/:owner/:repo/contents/docs/delivery-verification.md --jq .sha
```

### 4. Complete Verification Commands

A single command to verify everything:

```bash
# Verify file exists on main
git ls-remote --exit-code origin main:docs/delivery-verification.md && echo "File exists on main"

# Alternative check
git fetch origin main && git show origin/main:docs/delivery-verification.md > /dev/null && echo "File verified on main"
```

## Common Issues

| Issue | Resolution |
|-------|------------|
| PR not merged | Verify PR state with `gh pr view <pr-number>` |
| Commit not on main | Check if rebase was required before merge |
| File not found | Verify path is correct (case-sensitive on some systems) |

## Notes

- The `origin/main` reference is updated automatically when you `git fetch` or `git pull`
- Use `git fetch --all` to update all remote refs
- For large repos, `git show` may take a few seconds to execute